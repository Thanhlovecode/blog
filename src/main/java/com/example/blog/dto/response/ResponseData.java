package com.example.blog.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class ResponseData<T> {
    private int status;
    private String message;

    private LocalDateTime timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ResponseData<T> successWithMessage(String message, HttpStatus status) {
        return ResponseData.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .build();
    }

    public static <T> ResponseData<T> successWithData(String message, T data,HttpStatus status) {
        return ResponseData.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .data(data)
                .build();
    }

}
