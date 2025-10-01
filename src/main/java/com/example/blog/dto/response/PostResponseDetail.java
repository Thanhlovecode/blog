package com.example.blog.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDetail(
        String title,
        String slug,
        String username,
        String displayName,
        String thumbnailUrl,
        int readingTime,
        int totalViews,
        int totalComments,
        int totalLikes,
        String content,
        LocalDateTime publishedAt,
        List<CommentResponse> comments) {
}
