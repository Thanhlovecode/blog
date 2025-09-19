package com.example.blog.dto.request;


import com.example.blog.annotation.ValidPassword;
import jakarta.validation.constraints.NotNull;

public record PasswordRequest(
        @NotNull
        String email,

        @NotNull @ValidPassword
        String password,

        String confirmPassword) {
}
