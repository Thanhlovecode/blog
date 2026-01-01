package com.example.blog.scheduler;

import com.example.blog.service.RedisService;
import com.example.blog.service.ViewCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.blog.constants.CacheConstants.BATCH_SIZE;
import static com.example.blog.utils.RedisKeys.DIRTY_POST_SET_KEY;
import static com.example.blog.utils.RedisKeys.POST_VIEW_HASH_KEY;

@Slf4j(topic = "VIEW-SCHEDULE")
@Component
@RequiredArgsConstructor
public class ViewCountBatchSchedule {

    private static final String COUNTER_KEY = "post:views:counter";


    private final RedisService redisService;
    private final ViewCounterService viewCounterService;

    @Scheduled(fixedDelay = 60_000) // 60s
    public void batchUpdateViewCount(){
        log.info("Starting batch update view counts");

        List<Long> dirtyPostIds = redisService.popSetMembers(DIRTY_POST_SET_KEY, BATCH_SIZE);

        if(dirtyPostIds == null || dirtyPostIds.isEmpty()){
            log.info("No dirty post views to process");
            return;
        }

        Map<Long,Long> currentViews = redisService.getHashMultiGet(POST_VIEW_HASH_KEY, dirtyPostIds);


        if (!currentViews.isEmpty()) {
            viewCounterService.updateListPostViewCount(currentViews);
            log.info("Batch updated view counts for {} posts", currentViews.size());
        }
    }

}
