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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.blog.constants.CacheConstants.*;
import static com.example.blog.utils.RedisKeys.VIEW_COUNT_PREFIX;
import static com.example.blog.utils.RedisKeys.VIEW_COUNT_TTL;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "POST-CACHE-SERVICE")
public class PostCacheServiceImpl implements PostCacheService {

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
        List<String> cacheKeys = postIds.stream()
                .map(id -> CACHE_POST_METADATA + id)
                .toList();
        return redisService.multiGetPostMeta(cacheKeys, PostResponse.class);
    }

    @Override
    public Map<Long, Long> getListViewCountFromCache(List<Long> postIds) {
        // MGET view_count:123 view_count:124...
        List<String> viewKeys = postIds.stream()
                .map(id -> VIEW_COUNT_PREFIX + id)
                .toList();
        return redisService.multiGetViewCounts(viewKeys, postIds);
    }

    @Override
    public Long getViewCountRealTime(Long postId) {
        String value = redisService.getString(VIEW_COUNT_PREFIX + postId);
        return value != null ? Long.parseLong(value) : 0L;
    }

    @Override
    public void warmUpSingleViewCount(Long postId, long dbViewCount) {
        redisService.setStringWithTTL(
                VIEW_COUNT_PREFIX + postId,
                String.valueOf(dbViewCount),
                VIEW_COUNT_TTL);
        log.debug("Warm-up single view count for postId: {} = {}", postId, dbViewCount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_POST_DETAIL, key = "#slug", unless = "#result.getId() == null")
    public PostResponseDetail getCachedPostContent(String slug) {
        log.info("Cache Miss - Fetching Post from DB: {}", slug);
        Post post = findPostBySlugOrThrow(slug);
        List<CommentResponse> comments = commentService.getTop5CommentByPostId(post.getId());
        return postMapper.toPostResponseDetail(post, comments);
    }

    @Override
    public void warmUpViewCounts(Map<Long, Long> postIdViewCounts) {
        // Pipeline SET view_count:{postId} {count} EX 86400
        Map<String, String> keyValueMap = postIdViewCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> VIEW_COUNT_PREFIX + entry.getKey(),
                        entry -> entry.getValue().toString()));
        redisService.pipelineSetViewCounts(keyValueMap, VIEW_COUNT_TTL);
        log.debug("Warm-up set {} view counts", postIdViewCounts.size());
    }

    @Override
    public void multiSetPostResponses(List<PostResponse> postResponses) {
        Map<String, PostResponse> keyValueMap = postResponses.stream()
                .collect(Collectors.toMap(
                        postResponse -> CACHE_POST_METADATA + postResponse.getId(),
                        postResponse -> postResponse));
        redisService.multiSetPostMetaWithExpire(keyValueMap, EXPIRE_TIME, PostResponse.class);
    }

    private Post findPostBySlugOrThrow(String slug) {
        return postRepository.findPostWithContentBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }
}
