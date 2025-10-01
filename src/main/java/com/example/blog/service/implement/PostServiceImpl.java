package com.example.blog.service.implement;

import com.example.blog.domain.Post;
import com.example.blog.domain.PostContent;
import com.example.blog.domain.Profile;
import com.example.blog.domain.Tag;
import com.example.blog.dto.request.PostRequest;
import com.example.blog.dto.request.PostStatusUpdateRequest;
import com.example.blog.dto.request.PostUpdateRequest;
import com.example.blog.dto.response.CommentResponse;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.PostResponseDetail;
import com.example.blog.enums.ErrorCode;
import com.example.blog.enums.PostStatus;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.PostMapper;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.service.CommentService;
import com.example.blog.service.PostService;
import com.example.blog.service.RedisService;
import com.example.blog.utils.PageUtils;
import com.example.blog.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "POST-SERVICE")
public class PostServiceImpl implements PostService {

    private static final int WORDS_PER_MINUTE = 200;
    private static final int SLUG_RANDOM_LENGTH = 8;
    private static final int EXCERPT_MAX_LENGTH = 150;

    private static final String POST_CREATE_KEY = "idempotency:post:created:";
    private static final int POST_CREATE_KEY_EXPIRED = 1800; // SECOND


    private final ProfileRepository profileRepository;
    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final RedisService redisService;
    private final CommentService commentService;
    private final PostMapper postMapper;

    @Override
    public PageResponse<List<PostResponse>> getNewestPublishedPost(int page) {
        log.info("Request to get newest published posts, page: {}", page);
        Pageable pageable = PageUtils.defaultSortPageable(page);
        return getPostsPageResponse(
                ()-> postRepository.findNewestPostsByStatus(PostStatus.PUBLISHED,pageable)
        );
    }

    @Override
    public PageResponse<List<PostResponse>> getPublishedPostsByUsername(String username, int page) {
        log.info("Request to get published posts for username: {}", username);
        Pageable pageable = PageUtils.defaultSortPageable(page);
        return getPostsPageResponse(
                () -> postRepository.findPostIdsByUsernameAndStatus(username, PostStatus.PUBLISHED, pageable)
        );

    }

    @Override
    public PageResponse<List<PostResponse>> getPublishedPostsByTagSlug(String slug, int page, String sortBy) {
        log.info("Request to get published posts for tag slug: {}", slug);

        Pageable pageable = PageUtils.sortByFieldPageable(page, sortBy);

        return getPostsPageResponse(
                ()-> postRepository.findPostIdsByTagSlug(slug,PostStatus.PUBLISHED, pageable)
        );
    }


    private PageResponse<List<PostResponse>> getPostsPageResponse(Supplier<Page<Long>> supplier) {
        Page<Long> pageListPostId = supplier.get();

        if(pageListPostId.getContent().isEmpty()) {
            return null;
        }

        List<Post> posts = postRepository.findPostWithTagsByIds(pageListPostId.getContent()
                , pageListPostId.getSort());

        return PageResponse.fromPage(pageListPostId, convertToListPostResponse(posts));
    }


    @Override
    public PageResponse<List<PostResponse>> getPublishedPostsByKeySearch(String keyword, int page, String sortBy) {
        log.info("Request to get published posts for key word: {}", keyword);

        Pageable pageable = PageUtils.defaultNoSortPageable(page);

        Page<Long> pageListPostId = postRepository.findPostIdsByKeyword(keyword,PostStatus.PUBLISHED.name(), pageable);

        List<Post> posts = postRepository.findPostWithTagsByIds(pageListPostId.getContent(),
                                                                PageUtils.sortByField(sortBy));

        return PageResponse.fromPage(pageListPostId,convertToListPostResponse(posts));

    }

    private List<PostResponse> convertToListPostResponse(List<Post> posts) {
        return posts.stream()
                .map(postMapper::toPostResponse)
                .toList();
    }

    @Override
    public PostResponseDetail getPostDetailBySlug(String slug) {
        Post post = findPostBySlugOrThrow(slug);
        log.info("Get post by Slug: {}", post.getSlug());

        List<CommentResponse> comments = commentService.getTop5CommentByPostId(post.getId());
        return postMapper.toPostResponseDetail(post,comments);
    }

    @Override
    @Transactional
    @PreAuthorize("@postSecurity.isPostOwner(#slug)")
    public void updatePost(String slug, PostUpdateRequest updateRequest) {
        Post post = findPostBySlugOrThrow(slug);
        updatePostFields(post, updateRequest);
        postRepository.save(post);
        log.info("Update post with slug {}", post.getSlug());
    }


    @Override
    @Transactional
    @PreAuthorize("@postSecurity.isPostOwner(#slug)")
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

        log.info("Post status updated: slug='{}', from='{}' to='{}'",
                slug, currentStatus, newStatus);
    }


    @Override
    @Transactional
    public void createPost(PostRequest postRequest, String requestId) {
        String cacheKeyPost = POST_CREATE_KEY + requestId;
        if (!redisService.setStringIfAbsent(cacheKeyPost, "1", POST_CREATE_KEY_EXPIRED)) {
            return;
        }

        Profile profile = profileRepository.findByUserId(postRequest.userId()).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Post post = buildPost(postRequest, profile);

        postRepository.save(post);
        log.info("Created post successfully by userId: {} with title :{}", postRequest.userId(), post.getTitle());
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
                .displayName(profile.getUser().getFullName())
                .thumbnailUrl(profile.getThumbnailUrl())
                .tags(findAllTagsById(postRequest.idTags()))
                .readingTime(calculateReadingTime(postRequest.content()))
                .build();
        postContent.setPost(post);
        return post;
    }

    private String generateSlug(String title) {
        String slug = SlugUtil.toSlug(title);
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
        post.setTags(findAllTagsById(updateRequest.idTags()));
    }


}
