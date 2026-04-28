package com.example.blog.service.implement;

import com.example.blog.service.RedisService;
import com.example.blog.service.ViewCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.lettuce.core.RedisException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.example.blog.constants.CacheConstants.BATCH_SIZE;
import static com.example.blog.utils.RedisKeys.*;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "VIEW-SERVICE")
public class ViewCounterServiceImpl implements ViewCounterService {

    private final RedisService redisService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void recordViewCounter(Long postId, String clientIp, String userAgent) {
        try {
            String debounceKey = buildDebounceKey(postId, clientIp, userAgent);

            if (!redisService.setStringIfAbsent(debounceKey, "1", DEBOUNCE_SECONDS)) {
                return;
            }

            String viewKey = VIEW_COUNT_PREFIX + postId;
            redisService.incrementAndMarkDirty(viewKey, DIRTY_POST_SET_KEY, postId.toString());
            log.info("Record view counter for postId: {}", postId);
        } catch (RedisException e) {
            log.warn("RedisException occurred while recording view counter. Error: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateListPostViewCount(Map<Long, Long> postIncrements) {
        try {
            jdbcTemplate.batchUpdate(
                    "UPDATE posts SET total_views = GREATEST(total_views, ?) WHERE id = ?",
                    postIncrements.entrySet(),
                    BATCH_SIZE,
                    (ps, entry) -> {
                        ps.setLong(1, entry.getValue());
                        ps.setLong(2, entry.getKey());
                    });
        } catch (Exception e) {
            log.error("Failed to batch update view counts for {} posts. Error: {}",
                    postIncrements.size(), e.getMessage(), e);
        }
    }

    private String buildDebounceKey(Long postId, String clientIp, String userAgent) {
        String rawIdentity = clientIp + "_" + userAgent;
        String userIdentityHash = DigestUtils.md5Hex(rawIdentity);

        return String.format("%s:%d:%s",
                DEBOUNCE_KEY_PREFIX,
                postId,
                userIdentityHash);
    }
}
