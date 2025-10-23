package com.example.blog.event;

import com.example.blog.repository.PostRepository;
import com.example.blog.service.CloudinaryService;
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
import static com.example.blog.constants.CacheConstants.EXPIRE_TIME;

@Slf4j
@Component
@AllArgsConstructor
public class ApplicationEventListener {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService;
    private final ViewCounterService viewCounterService;
    private final RedisService redisService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateImageUser(ProfileImageUpdateEvent event) {

        postRepository.updateUserThumbnailUrl(event.getUserId(), event.getThumbnailUrl());
        log.info("Posts of user with id {} updated successfully", event.getUserId());
    }

    @Async("taskExecutor")
    @EventListener
    public void handleImageCleanup(ImageCleanUpEvent event){
        cloudinaryService.deleteImage(event.getImageId());
    }


    @Async("taskExecutor")
    @EventListener
    public void handleCountPostView(PostViewEvent event){
        viewCounterService.recordViewCounter(event.userId(), event.postId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    public void handleUpdateCachePostResponse(PostUpdateEvent event){
        String cachePostMetadataKey = CACHE_POST_METADATA + event.postResponse().getId();
        redisService.setObject(cachePostMetadataKey, event.postResponse(), EXPIRE_TIME);
    }

}
