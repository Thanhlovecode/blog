package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RateLimitResponse {
    private final boolean allowed;
    private final long retryAfterSeconds;

    public static RateLimitResponse allowed() {
        return new RateLimitResponse(true, 0);
    }

    public static RateLimitResponse rejected(long retryAfterSeconds) {
        return new RateLimitResponse(false, retryAfterSeconds);
    }
}
