package com.example.blog.dto.response;

import java.time.LocalDateTime;

public record PostResponseDetail(
        String title,
        String slug,
        String username,
        String thumbnailUrl,
        int readingTime,
        int totalViews,
        int totalComments,
        int totalLikes,
        String content,
        LocalDateTime publishedAt) {
}
