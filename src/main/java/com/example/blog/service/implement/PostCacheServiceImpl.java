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

import static com.example.blog.constants.CacheConstants.CACHE_POST_METADATA;
import static com.example.blog.constants.CacheConstants.POST_IDS_PAGE_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
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
        List<String> cacheKeys = buildListCacheKeys(postIds);
        return redisService.multiGetPostResponses(cacheKeys);
    }

    @Override
    public void multiSetPostResponses(List<PostResponse> postResponses) {
        Map<String, Object> keyValueMap = postResponses.stream()
                .collect(Collectors.toMap(
                        postResponse -> CACHE_POST_METADATA + postResponse.getId(),
                        postResponse -> postResponse
                ));
        redisService.multiSetPostResponses(keyValueMap,EXPIRE_TIME);
    }

    private List<String> buildListCacheKeys(List<Long> postIds) {
        return postIds.stream()
                .map(id -> CACHE_POST_METADATA + id)
                .toList();
    }
}
