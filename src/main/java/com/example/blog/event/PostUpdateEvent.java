package com.example.blog.event;

import com.example.blog.dto.response.PostResponse;

public record PostUpdateEvent(PostResponse postResponse) {
}
