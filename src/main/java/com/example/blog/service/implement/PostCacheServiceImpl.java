package com.example.blog.service.implement;

import com.example.blog.domain.Post;
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
import com.example.blog.service.CommentService;
import com.example.blog.service.PostCacheService;
import com.example.blog.service.RedisService;
import com.example.blog.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.blog.constants.CacheConstants.*;
import static com.example.blog.utils.RedisKeys.POST_VIEW_HASH_KEY;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "POST-CACHE-SERVICE")
public class PostCacheServiceImpl implements PostCacheService {


    private static final Integer EXPIRE_TIME = 3600; // 1 hour
    private final RedisService redisService;
    private final PostRepository postRepository;
    private final CommentService commentService;
    private final PostMapper postMapper;

    @Override
    @Cacheable(cacheNames = POST_IDS_PAGE_CACHE, key = "#page", condition = "#page <= 10")
    public PageResponse<Long> getPostIdsPage(int page) {
        Pageable pageable = PageUtils.defaultSortPageable(page);
        log.info("Cache miss for newest posts page {}", page);

        Page<Long> pagePostIds = postRepository.findNewestPostsByStatus(PostStatus.PUBLISHED, pageable);
        return PageResponse.buildPage(pagePostIds);
    }

    @Override
    public List<PostResponse> getListPostResponseFromCache(List<Long> postIds) {
        List<String> cacheKeys = buildListCacheKeys(postIds);
        return redisService.multiGetValues(cacheKeys,PostResponse.class);
    }

    @Override
    public Map<Long,Long> getListViewCountFromCache(List<Long> postIds) {
       return redisService.getHashMultiGet(POST_VIEW_HASH_KEY, postIds);
    }

    @Override
    public void multiSetViewCounts(List<PostResponse> postResponses) {
        Map<String, Long> viewCountMap = postResponses.stream()
                .collect(Collectors.toMap(
                        postResponse -> CACHE_POST_VIEW_COUNT + postResponse.getId(),
                         PostResponse::getTotalViews
                ));
        redisService.multiSetWithExpire(viewCountMap, EXPIRE_TIME);
    }

    @Override
    public Long getViewCountRealTime(Long postId) {
        return redisService.getViewCountRealTime(postId);
    }

    private Post findPostBySlugOrThrow(String slug) {
        return postRepository.findPostWithContentBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }



    @Override
    @Cacheable(cacheNames = CACHE_POST_DETAIL, key = "#slug")
//    unless = "#result == null || #result.publishedAt.isBefore(T(java.time.LocalDateTime).now().minusDays(7))")
    public PostResponseDetail getCachedPostContent(String slug) {
        log.info("Cache Miss - Fetching Post from DB: {}", slug);
        Post post = findPostBySlugOrThrow(slug);
        List<CommentResponse> comments = commentService.getTop5CommentByPostId(post.getId());

        return postMapper.toPostResponseDetail(post, comments);
    }

    @Override
    public void hashMultiSetViewCounts(Map<Long, Long> postIdViewCounts) {
        Map<String,String> stringKeyMap = postIdViewCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()
                ));

        redisService.hashMultiSet(POST_VIEW_HASH_KEY, stringKeyMap);
        log.debug("Batch set {} view counts", postIdViewCounts.size());
    }

    @Override
    public void multiSetPostResponses(List<PostResponse> postResponses) {
        Map<String, Object> keyValueMap = postResponses.stream()
                .collect(Collectors.toMap(
                        postResponse -> CACHE_POST_METADATA + postResponse.getId(),
                        postResponse -> postResponse
                ));
        redisService.multiSetWithExpire(keyValueMap,EXPIRE_TIME);
    }

    private List<String> buildListCacheKeys(List<Long> postIds) {
        return postIds.stream()
                .map(id -> CACHE_POST_METADATA + id)
                .toList();
    }
}
