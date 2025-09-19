package com.example.blog.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ImageCleanUpEvent extends ApplicationEvent {
    private final String imageId;
    public ImageCleanUpEvent(Object source,String imageId) {
        super(source);
        this.imageId = imageId;
    }
}
