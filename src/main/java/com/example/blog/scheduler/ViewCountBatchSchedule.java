package com.example.blog.scheduler;

import com.example.blog.service.RedisService;
import com.example.blog.service.ViewCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.example.blog.constants.CacheConstants.BATCH_SIZE;
import static com.example.blog.constants.CacheConstants.CACHE_POST_VIEW_COUNT;

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
        Set<ZSetOperations.TypedTuple<Object>> entries = redisService.popMinFromSortedSet(COUNTER_KEY, BATCH_SIZE);

        if(entries == null || entries.isEmpty()){
            log.info("No pending view counts to update");
            return;
        }

        Map<String, Integer> keyIncrements = new HashMap<>();
        Map<Long, Integer> postIncrements = new HashMap<>();

        for(ZSetOperations.TypedTuple<Object> entry : entries){
            Long postId = Long.parseLong(entry.getValue().toString());
            Integer viewIncrement = entry.getScore().intValue();
            keyIncrements.put(CACHE_POST_VIEW_COUNT + postId, viewIncrement);
            postIncrements.put(postId, viewIncrement);
        }

        // Chỉ batch update trong transaction
        viewCounterService.updateListPostViewCount(postIncrements);


        redisService.multiIncrementKeys(keyIncrements);
        log.info("=== Batch update finished. Processed {} entries ===", entries.size());
    }

}
