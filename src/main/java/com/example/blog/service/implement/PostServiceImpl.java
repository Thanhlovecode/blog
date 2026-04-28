package com.example.blog.service.implement;

import com.example.blog.domain.Post;
import com.example.blog.domain.PostContent;
import com.example.blog.domain.Profile;
import com.example.blog.domain.Tag;
import com.example.blog.dto.request.PostRequest;
import com.example.blog.dto.request.PostStatusUpdateRequest;
import com.example.blog.dto.request.PostUpdateRequest;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.PostResponseDetail;
import com.example.blog.enums.ErrorCode;
import com.example.blog.enums.PostStatus;
import com.example.blog.event.PostPublishedEvent;
import com.example.blog.event.PostUpdateEvent;
import com.example.blog.event.WarmUpViewCountsEvent;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.PostMapper;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.service.CommentService;
import com.example.blog.service.PostCacheService;
import com.example.blog.service.PostService;
import com.example.blog.utils.PageUtils;
import com.example.blog.utils.SecurityUtils;
import com.example.blog.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailParseException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.example.blog.constants.CacheConstants.CACHE_POST_DETAIL;
import static com.example.blog.constants.CacheConstants.POST_IDS_PAGE_CACHE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "POST-SERVICE")
public class PostServiceImpl implements PostService {

    private static final int WORDS_PER_MINUTE = 200;
    private static final int SLUG_RANDOM_LENGTH = 8;
    private static final int EXCERPT_MAX_LENGTH = 150;

    private final ProfileRepository profileRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final PostCacheService postCacheService;
    private final ApplicationEventPublisher publisher;
    private final CommentService commentService;
    private final PostMapper postMapper;

    @Override
    public PageResponse<PostResponse> getNewestPublishedPost(int page) {
        log.info("Request to get newest published posts, page: {}", page);
        PageResponse<Long> pageListPostId = postCacheService.getPostIdsPage(page);

        List<Long> postIds = pageListPostId.getContent();
        if (postIds.isEmpty()) {
            return PageResponse.empty();
        }

        List<PostResponse> cachePostResponse = getPostResponses(postIds, page);

        updateRealTimeViewCounts(cachePostResponse, postIds);

        return pageListPostId.map(cachePostResponse);

    }

    private List<PostResponse> getPostResponses(List<Long> postIds, int page) {
        List<PostResponse> cached = postCacheService.getListPostResponseFromCache(postIds);

        if (!cached.isEmpty() && cached.size() == postIds.size()) {
            log.info("Cache hit for post responses on page {}", page);
            return cached;
        }

        log.info("Cache miss for post responses on page {}", page);
        List<Post> posts = postRepository.findPostWithTagsByIds(postIds, PageUtils.sortDefault());
        List<PostResponse> postResponses = convertToListPostResponse(posts);

        postCacheService.multiSetPostResponses(postResponses);

        return postResponses;
    }

    private void updateRealTimeViewCounts(List<PostResponse> cachePostResponse, List<Long> postIds) {

        Map<Long, Long> warmUpViewCounts = new HashMap<>();
        Map<Long, Long> realTimeViewCounts = postCacheService.getListViewCountFromCache(postIds);

        for (PostResponse postResponse : cachePostResponse) {
            Long realTimeViewCount = realTimeViewCounts.get(postResponse.getId());
            if (realTimeViewCount != null) {
                postResponse.setTotalViews(realTimeViewCount);
            } else {
                warmUpViewCounts.put(postResponse.getId(), postResponse.getTotalViews());
            }
        }
        if (!warmUpViewCounts.isEmpty()) {
            publisher.publishEvent(new WarmUpViewCountsEvent(warmUpViewCounts));
        }

    }

    @Override
    public PageResponse<PostResponse> getPublishedPostsByUsername(String username, int page) {
        log.info("Request to get published posts for username: {}", username);
        Pageable pageable = PageUtils.defaultSortPageable(page);
        return getPostsPageResponse(
                () -> postRepository.findPostIdsByUsernameAndStatus(username, PostStatus.PUBLISHED, pageable));

    }

    @Override
    public PageResponse<PostResponse> getPublishedPostsByTagSlug(String slug, int page, String sortBy) {
        log.info("Request to get published posts for tag slug: {}", slug);

        Pageable pageable = PageUtils.sortByFieldPageable(page, sortBy);

        return getPostsPageResponse(
                () -> postRepository.findPostIdsByTagSlug(slug, PostStatus.PUBLISHED, pageable));
    }

    private PageResponse<PostResponse> getPostsPageResponse(Supplier<Page<Long>> supplier) {
        Page<Long> pageListPostId = supplier.get();

        if (pageListPostId.getContent().isEmpty()) {
            return null;
        }

        List<Post> posts = postRepository.findPostWithTagsByIds(pageListPostId.getContent(), pageListPostId.getSort());

        return PageResponse.fromPage(pageListPostId, convertToListPostResponse(posts));
    }

    @Override
    public PageResponse<PostResponse> getPublishedPostsByKeySearch(String keyword, int page, String sortBy) {
        log.info("Request to get published posts for key word: {}", keyword);

        Pageable pageable = PageUtils.defaultNoSortPageable(page);

        Page<Long> pageListPostId = postRepository.findPostIdsByKeyword(keyword, PostStatus.PUBLISHED.name(), pageable);

        List<Post> posts = postRepository.findPostWithTagsByIds(pageListPostId.getContent(),
                PageUtils.sortByField(sortBy));

        return PageResponse.fromPage(pageListPostId, convertToListPostResponse(posts));

    }

    private List<PostResponse> convertToListPostResponse(List<Post> posts) {
        return posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
    }

    @Override
    public PostResponseDetail getPostDetailBySlug(String slug, String clientIp) {
        PostResponseDetail postResponseDetail = postCacheService.getCachedPostContent(slug);
        Long viewCountForPost = postCacheService.getViewCountRealTime(postResponseDetail.getId());

        if (viewCountForPost == 0) {
            postCacheService.warmUpSingleViewCount(
                    postResponseDetail.getId(), postResponseDetail.getTotalViews());
            viewCountForPost = postResponseDetail.getTotalViews();
        }
        postResponseDetail.setTotalViews(viewCountForPost);
        return postResponseDetail;

    }

    @Override
    @Transactional
    @PreAuthorize("@postSecurity.isPostOwner(#slug)")
    @CacheEvict(cacheNames = CACHE_POST_DETAIL, key = "#slug")
    public void updatePost(String slug, PostUpdateRequest updateRequest) {
        Post post = findPostBySlugOrThrow(slug);
        updatePostFields(post, updateRequest);
        postRepository.save(post);
        publisher.publishEvent(new PostUpdateEvent(postMapper.toPostResponse(post)));
        log.info("Update post with slug {}", post.getSlug());
    }

    @Override
    @Transactional
    @PreAuthorize("@postSecurity.isPostOwner(#slug)")
    @CacheEvict(cacheNames = { POST_IDS_PAGE_CACHE, CACHE_POST_DETAIL }, allEntries = true)
    public void updateStatusPost(String slug, PostStatusUpdateRequest statusUpdateRequest) {
        Post post = postRepository.findPostWithoutContentBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        PostStatus currentStatus = post.getStatus();
        PostStatus newStatus = statusUpdateRequest.status();

        if (currentStatus == newStatus) {
            return;
        }

        handlePublishedDateUpdate(post, currentStatus, newStatus);
        post.setStatus(newStatus);
        postRepository.save(post);

        // Giai đoạn 1: Publish & Warm-up — fire event AFTER_COMMIT
        if (newStatus == PostStatus.PUBLISHED) {
            publisher.publishEvent(new PostPublishedEvent(post.getId(), postMapper.toPostResponse(post)));
        }

        log.info("Post status updated: slug='{}', from='{}' to='{}'",
                slug, currentStatus, newStatus);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(cacheNames = POST_IDS_PAGE_CACHE, allEntries = true)
    public void createPost(PostRequest postRequest) {
        long userId = SecurityUtils.getCurrentUserId();
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = buildPost(postRequest, profile);

        postRepository.save(post);
        log.info("Created post successfully by username: {} with title :{}", post.getDisplayName(), post.getTitle());
    }

    private Set<Tag> findAllTagsById(Set<Long> ids) {
        return tagRepository.findAllByIdIn(ids);
    }

    private int calculateReadingTime(String content) {
        int estimatedWords = content.length() / 6;
        double minutes = (double) estimatedWords / WORDS_PER_MINUTE;
        return Math.max(1, (int) Math.ceil(minutes));
    }

    private PostContent buildPostContent(String content) {
        return PostContent.builder()
                .content(content.trim())
                .build();
    }

    private Post buildPost(PostRequest postRequest, Profile profile) {
        PostContent postContent = buildPostContent(postRequest.content());
        Post post = Post.builder()
                .title(postRequest.title())
                .slug(generateSlug(postRequest.title()))
                .excerpt(postRequest.content().substring(0, EXCERPT_MAX_LENGTH))
                .postContent(postContent)
                .user(profile.getUser())
                .status(postRequest.status())
                .username(profile.getUser().getUsername())
                .displayName(profile.getFullName())
                .thumbnailUrl(profile.getThumbnailUrl())
                .tags(findAllTagsById(postRequest.idTags()))
                .readingTime(calculateReadingTime(postRequest.content()))
                .build();
        postContent.setPost(post);
        return post;
    }

    private String generateSlug(String title) {
        String slug = SlugUtils.toSlug(title);
        String randomString = UUID.randomUUID().toString().substring(0, SLUG_RANDOM_LENGTH);
        return slug + "-" + randomString;
    }

    private void handlePublishedDateUpdate(Post post, PostStatus currentStatus, PostStatus newStatus) {
        if (newStatus == PostStatus.PUBLISHED && currentStatus != PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
            log.info("Post '{}' published at {}", post.getTitle(), post.getPublishedAt());
        }
    }

    private Post findPostBySlugOrThrow(String slug) {
        return postRepository.findPostWithContentBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    private void updatePostFields(Post post, PostUpdateRequest updateRequest) {
        if (!post.getTitle().equals(updateRequest.title())) {
            post.setTitle(updateRequest.title());
            post.setSlug(generateSlug(updateRequest.title()));
        }
        post.getPostContent().setContent(updateRequest.content());
        post.setReadingTime(calculateReadingTime(updateRequest.content()));
        post.setExcerpt(updateRequest.content().substring(0, EXCERPT_MAX_LENGTH));
        post.setTags(findAllTagsById(updateRequest.idTags()));
    }

}
