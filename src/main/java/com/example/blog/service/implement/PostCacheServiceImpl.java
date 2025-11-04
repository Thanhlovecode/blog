package com.example.blog.service.implement;

import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.enums.PostStatus;
import com.example.blog.repository.PostRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j(topic = "POST-CACHE-SERVICE")
public class PostCacheServiceImpl implements PostCacheService {


    private static final Integer EXPIRE_TIME = 3600; // 1 hour
    private final RedisService redisService;
    private final PostRepository postRepository;

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
        List<String> cacheKeys = buildListCacheKeys(postIds,CACHE_POST_METADATA);
        return redisService.multiGetValues(cacheKeys,PostResponse.class);
    }

    @Override
    public List<Integer> getListViewCountFromCache(List<Long> postIds) {
        List<String> cacheKeys = buildListCacheKeys(postIds,CACHE_POST_VIEW_COUNT);
        return redisService.multiGetValues(cacheKeys,Integer.class);
    }

    @Override
    public void multiSetViewCounts(List<PostResponse> postResponses) {
        Map<String, Integer> viewCountMap = postResponses.stream()
                .collect(Collectors.toMap(
                        postResponse -> CACHE_POST_VIEW_COUNT + postResponse.getId(),
                         PostResponse::getTotalViews
                ));
        redisService.multiSetWithExpire(viewCountMap, EXPIRE_TIME);
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

    private List<String> buildListCacheKeys(List<Long> postIds,String cacheKeyPrefix) {
        return postIds.stream()
                .map(id -> cacheKeyPrefix + id)
                .toList();
    }
}
