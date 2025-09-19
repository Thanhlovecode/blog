package com.example.blog.dto.response;

import com.example.blog.enums.PostStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
public class PostResponse {
    private String title;
    private String slug;
    private String excerpt;
    private String username;
    private int readingTime;
    private String thumbnailUrl;
    private int totalComments;
    private int totalViews;
    private PostStatus status;
    private LocalDateTime publishedAt;
    private Set<String> tags;
}
