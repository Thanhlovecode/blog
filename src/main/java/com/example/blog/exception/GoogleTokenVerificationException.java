package com.example.blog.exception;

import com.example.blog.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GoogleTokenVerificationException extends RuntimeException {
    private final HttpStatus status;
    public GoogleTokenVerificationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.status = errorCode.getHttpStatus();
    }
}
