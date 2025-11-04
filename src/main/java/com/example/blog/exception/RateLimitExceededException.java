package com.example.blog.exception;

import lombok.Getter;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message)
    {
        super(message);
    }

}
