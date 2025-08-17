package com.example.blog.controller;

import com.example.blog.dto.request.EmailRequest;
import com.example.blog.dto.request.OTPRequest;
import com.example.blog.dto.request.PasswordRequest;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/password")
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/email")
    public ResponseEntity<ResponseData<Void>> requestOtp(@Valid @RequestBody EmailRequest emailRequest) {
        passwordService.sendOTP(emailRequest);
        return ResponseEntity.ok(ResponseData.successWithMessage
                ("Send Otp successfully", HttpStatus.OK));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ResponseData<Void>> verifyOtp(@Valid @RequestBody OTPRequest otpRequest) {
        passwordService.verifyOtp(otpRequest);
        return ResponseEntity.ok(ResponseData.successWithMessage
                ("OTP verified successfully", HttpStatus.OK));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ResponseData<Void>> changePassword(@Valid @RequestBody PasswordRequest passwordRequest) {
        passwordService.changePassword(passwordRequest);
        return ResponseEntity.ok(ResponseData.successWithMessage
                ("Password changed successfully", HttpStatus.OK));
    }
}




