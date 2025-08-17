package com.example.blog.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@Builder
public class ResponseData<T> {
    private int status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ResponseData<T> successWithMessage(String message, HttpStatus status) {
        return ResponseData.<T>builder()
                .status(status.value())
                .message(message)
                .build();
    }

    public static <T> ResponseData<T> successWithData(String message, T data) {
        return ResponseData.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

}
