package com.example.blog.dto.response;

import com.example.blog.enums.PostStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String username;
    private String displayName;
    private int readingTime;
    private String thumbnailUrl;
    private int totalComments;

    private int totalViews;
    private PostStatus status;
    private LocalDateTime publishedAt;
    private Set<String> tags;
}
