package com.example.blog.dto.request;


import com.example.blog.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotNull @Email String email,
        @NotNull @ValidPassword String password) {
}
