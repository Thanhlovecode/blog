package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "ID token is required")
        String idToken
) {
}
