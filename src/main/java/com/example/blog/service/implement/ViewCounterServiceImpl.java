package com.example.blog.service.implement;

import com.example.blog.service.RedisService;
import com.example.blog.service.ViewCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "VIEW-SERVICE")
public class ViewCounterServiceImpl implements ViewCounterService {

    private final RedisService redisService;

    private static final int DEBOUNCE_SECONDS = 600; //5 minutes
    private static final String DEBOUNCE_KEY_PREFIX = "post:view:debounce:";
    private static final String COUNTER_KEY = "post:views:counter";

    @Override
    public void recordViewCounter(Long userId, Long postId) {
        String debounceKey = String.format("%s%d:%d", DEBOUNCE_KEY_PREFIX, userId, postId);
        if(!redisService.setStringIfAbsent(debounceKey,"1", DEBOUNCE_SECONDS)) {
            return ;
        }

        redisService.incrementSortedSetScore(COUNTER_KEY, postId.toString(), 1);
        log.info("View recorded - Post: {}, User: {}", postId, userId);
    }
}
