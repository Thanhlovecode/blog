package com.example.blog.dto.request;


import jakarta.validation.constraints.NotNull;

public record OTPRequest(
        @NotNull
        String email,
        @NotNull
        String otp){
}
