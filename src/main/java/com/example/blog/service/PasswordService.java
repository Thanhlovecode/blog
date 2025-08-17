package com.example.blog.service;

import com.example.blog.dto.request.EmailRequest;
import com.example.blog.dto.request.OTPRequest;
import com.example.blog.dto.request.PasswordRequest;

public interface PasswordService {
    void verifyOtp(OTPRequest otpRequest);
    void sendOTP(EmailRequest emailRequest);
    void changePassword(PasswordRequest passwordRequest);

}
