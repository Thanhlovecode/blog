package com.example.blog.scheduler;

import com.example.blog.service.RedisService;
import com.example.blog.service.ViewCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.example.blog.constants.CacheConstants.BATCH_SIZE;
import static com.example.blog.utils.RedisKeys.DIRTY_POST_SET_KEY;
import static com.example.blog.utils.RedisKeys.VIEW_COUNT_PREFIX;

@Slf4j(topic = "VIEW-SCHEDULE")
@Component
@RequiredArgsConstructor
public class ViewCountBatchSchedule {

    private final RedisService redisService;
    private final ViewCounterService viewCounterService;

    @Scheduled(fixedDelay = 30_000) // 30s
    public void batchUpdateViewCount() {
        log.info("Starting batch update view counts");

        List<Long> dirtyPostIds = redisService.popSetMembers(DIRTY_POST_SET_KEY, BATCH_SIZE);

        if (dirtyPostIds == null || dirtyPostIds.isEmpty()) {
            log.info("No dirty post views to process");
            return;
        }

        // Build MGET keys: view_count:123, view_count:124...
        List<String> viewKeys = dirtyPostIds.stream()
                .map(id -> VIEW_COUNT_PREFIX + id)
                .toList();

        // MGET view_count:123 view_count:124
        Map<Long, Long> currentViews = redisService.multiGetViewCounts(viewKeys, dirtyPostIds);

        if (!currentViews.isEmpty()) {
            viewCounterService.updateListPostViewCount(currentViews);
            log.info("Batch updated view counts for {} posts", currentViews.size());
        }
    }
}
