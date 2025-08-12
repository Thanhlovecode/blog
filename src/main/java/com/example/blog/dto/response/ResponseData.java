package com.example.blog.dto.response;

import lombok.*;

@Setter
@Getter
@Builder
public class ResponseData {
    private int status;
    private String message;
}
