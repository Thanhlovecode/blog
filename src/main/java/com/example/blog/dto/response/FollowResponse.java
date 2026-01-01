package com.example.blog.dto.response;


public record FollowResponse(
        String displayName,
        String thumbnailUrl,
        Long userId
) {
}
