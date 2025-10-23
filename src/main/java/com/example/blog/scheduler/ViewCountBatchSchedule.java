package com.example.blog.scheduler;

import com.example.blog.repository.PostRepository;
import com.example.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountBatchSchedule {

    private static final String COUNTER_KEY = "post:views:counter";
    private static final int BATCH_SIZE = 100;  // can change

    private final PostRepository postRepository;
    private final RedisService redisService;

    @Scheduled(fixedDelay = 30_000) // 30s
    @Transactional
    public void batchUpdateViewCount(){
        log.info("Starting batch update view counts");
        Set<ZSetOperations.TypedTuple<Object>> entries = redisService.popMinFromSortedSet(COUNTER_KEY, BATCH_SIZE);

        if(entries == null || entries.isEmpty()){
            log.debug("No pending view counts to update");
            return;
        }
        for(ZSetOperations.TypedTuple<Object> entry : entries){
            Long postId = Long.parseLong(entry.getValue().toString());
            Long viewIncrement = entry.getScore().longValue();
            postRepository.incrementViewCount(postId, viewIncrement);
            log.debug("Updated postId={} with increment={}", postId, viewIncrement);
        }
        log.info("=== Batch update finished. Processed {} entries ===", entries.size());
    }


}
