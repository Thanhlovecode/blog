package com.example.blog.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public class ProfileImageUpdateEvent extends ApplicationEvent {
    private final String thumbnailUrl;
    private final Long userId;
    public ProfileImageUpdateEvent(Object source, String thumbnailUrl, Long userId) {
        super(source);
        this.thumbnailUrl = thumbnailUrl;
        this.userId = userId;
    }
}
