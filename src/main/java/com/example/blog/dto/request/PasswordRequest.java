package com.example.blog.dto.request;


import com.example.blog.validator.ValidPassword;
import jakarta.validation.constraints.NotNull;

public record PasswordRequest(
        @NotNull
        String email,

        @NotNull @ValidPassword
        String password,

        String confirmPassword) {
}
