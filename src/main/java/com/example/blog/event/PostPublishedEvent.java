package com.example.blog.event;

import com.example.blog.dto.response.PostResponse;

public record PostPublishedEvent(Long postId, PostResponse postResponse) {
}
