package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Builder
@Getter
public class ErrorResponse {
    private int code;
    private ZonedDateTime timestamp;
    private String path;
    private String error;
    private String message;
}
