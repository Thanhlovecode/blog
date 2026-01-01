package com.example.blog.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostResponseDetail {
    private Long id;
    private String title;
    private String slug;
    private String username;
    private String displayName;
    private String thumbnailUrl;
    private int readingTime;
    private long totalViews;
    private int totalComments;
    private int totalLikes;
    private String content;
    private LocalDateTime publishedAt;
    private List<CommentResponse> comments;

}
