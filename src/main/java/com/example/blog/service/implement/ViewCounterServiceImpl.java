package com.example.blog.service.implement;

import com.example.blog.service.RedisService;
import com.example.blog.service.ViewCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.example.blog.constants.CacheConstants.BATCH_SIZE;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "VIEW-SERVICE")
public class ViewCounterServiceImpl implements ViewCounterService {

    private final RedisService redisService;
    private final JdbcTemplate jdbcTemplate;

    private static final int DEBOUNCE_SECONDS = 600; //5 minutes
    private static final String DEBOUNCE_KEY_PREFIX = "post:view:debounce:";
    private static final String COUNTER_KEY = "post:views:counter";

    @Override
    public void recordViewCounter(Long postId, String clientIp) {
        String debounceKey = buildDebounceKey(postId, clientIp);

        if (!redisService.setStringIfAbsent(debounceKey, "1", DEBOUNCE_SECONDS)) {
            return;
        }

        redisService.incrementSortedSetScore(COUNTER_KEY, postId.toString(), 1);
        log.info("record view counter for postId: {}", postId);
    }


    @Override
    @Transactional
    public void updateListPostViewCount(Map<Long, Integer> postIncrements) {
        jdbcTemplate.batchUpdate(
                "UPDATE posts SET total_views = total_views + ? WHERE id = ?",
                postIncrements.entrySet(),
                BATCH_SIZE,
                (ps, entry) -> {
                    ps.setInt(1, entry.getValue());
                    ps.setLong(2, entry.getKey());
                }
        );
    }

    private String buildDebounceKey(Long postId, String clientIp) {
        return DEBOUNCE_KEY_PREFIX + postId + ":" + clientIp;
    }
}
