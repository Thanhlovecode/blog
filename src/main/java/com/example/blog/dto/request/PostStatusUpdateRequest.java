package com.example.blog.dto.request;

import com.example.blog.enums.PostStatus;
import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateRequest(
        @NotNull(message = "Status is required")
        PostStatus status
) {
}
