package com.example.blog.event;

import com.example.blog.repository.PostRepository;
import com.example.blog.service.CloudinaryService;
import com.example.blog.service.PostCacheService;
import com.example.blog.service.RedisService;
import com.example.blog.service.ViewCounterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.example.blog.constants.CacheConstants.CACHE_POST_METADATA;
import static com.example.blog.utils.RedisKeys.VIEW_COUNT_PREFIX;
import static com.example.blog.utils.RedisKeys.VIEW_COUNT_TTL;

@Slf4j
@Component
@AllArgsConstructor
public class ApplicationEventListener {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService;
    private final ViewCounterService viewCounterService;
    private final RedisService redisService;
    private final PostCacheService postCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("virtualThreadExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateImageUser(ProfileImageUpdateEvent event) {
        postRepository.updateUserThumbnailUrl(event.getUserId(), event.getThumbnailUrl());
        log.info("Posts of user with id {} updated successfully", event.getUserId());
    }

    @Async("virtualThreadExecutor")
    @EventListener
    public void handleImageCleanup(ImageCleanUpEvent event) {
        cloudinaryService.deleteImage(event.getImageId());
    }

    // ──────────────────────────────────────────────
    // Giai đoạn 1: Publish & Warm-up
    // AFTER_COMMIT → Worker Thread SET Redis keys
    // ──────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("virtualThreadExecutor")
    public void handlePostPublished(PostPublishedEvent event) {
        // SET post:meta:{postId} "{title, content...}" EX 86400
        String metaKey = CACHE_POST_METADATA + event.postId();
        redisService.setPostMeta(metaKey, event.postResponse(), VIEW_COUNT_TTL);

        // SET view_count:{postId} 0 EX 86400
        String viewKey = VIEW_COUNT_PREFIX + event.postId();
        redisService.setStringWithTTL(viewKey, "0", VIEW_COUNT_TTL);

        log.info("Warm-up completed for postId: {} — post:meta and view_count seeded", event.postId());
    }

    // ──────────────────────────────────────────────
    // Giai đoạn 2: Đếm View nền (Async Write)
    // Main thread ném PostViewEvent → Background thread INCR + SADD
    // ──────────────────────────────────────────────

    @Async("virtualThreadExecutor")
    @EventListener
    public void handleCountPostView(PostViewEvent event) {
        viewCounterService.recordViewCounter(event.postId(), event.clientIp(), event.userAgent());
    }

    // ──────────────────────────────────────────────
    // Post update: refresh metadata cache
    // ──────────────────────────────────────────────

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("virtualThreadExecutor")
    public void handleUpdateCachePostResponse(PostUpdateEvent event) {
        String cachePostMetadataKey = CACHE_POST_METADATA + event.postResponse().getId();
        redisService.setPostMeta(cachePostMetadataKey, event.postResponse(), VIEW_COUNT_TTL);
    }

    // ──────────────────────────────────────────────
    // Warm-up view counts from DB for cache misses
    // ──────────────────────────────────────────────

    @Async("virtualThreadExecutor")
    @EventListener
    public void handleWarmUpViewCounts(WarmUpViewCountsEvent event) {
        postCacheService.warmUpViewCounts(event.postIdViewCounts());
    }
}
