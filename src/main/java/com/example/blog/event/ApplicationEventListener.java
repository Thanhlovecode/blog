package com.example.blog.event;

import com.example.blog.repository.PostRepository;
import com.example.blog.service.CloudinaryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@AllArgsConstructor
public class ApplicationEventListener {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService;

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

}
