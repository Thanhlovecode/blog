package com.example.blog.dto.request;

import com.example.blog.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PostUpdateRequest(
        @NotBlank(message = "title is required")
        @Size(min = 5, max = 255)
        String title,

        @NotBlank(message = "Content is required")
        @Size(min = 200, message = "Post content is very short, minimum 200 characters required")
        String content,

        @Size(min = 1, max = 10, message = "At least one tag is required")
        Set<Long> idTags) {
}
