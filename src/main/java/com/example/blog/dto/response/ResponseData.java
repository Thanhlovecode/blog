package com.example.blog.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@Builder
public class ResponseData<T> {
    private int status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}
