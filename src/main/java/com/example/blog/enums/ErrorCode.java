package com.example.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@AllArgsConstructor
@Getter
public enum ErrorCode {

    EMAIL_EXISTED("EMAIL EXISTED", HttpStatus.CONFLICT);
    private final String message;
    private final HttpStatus httpStatus;


}
