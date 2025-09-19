package com.example.blog.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TagRequest(

        @NotNull
        @Size(min = 2, max = 100)
        String name) {
}
