package com.example.blog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@AllArgsConstructor
@Getter
public enum ErrorCode {

    EMAIL_EXISTED("EMAIL EXISTED", HttpStatus.CONFLICT),
    USER_NOT_EXISTED("User not existed",HttpStatus.NOT_FOUND),
    OTP_INCORRECT("OTP incorrect or expire", HttpStatus.UNPROCESSABLE_ENTITY),
    PASSWORD_NOT_MATCH("Password not match", HttpStatus.UNPROCESSABLE_ENTITY),
    EMAIL_NOT_VERIFY_OTP("Email not verify otp", HttpStatus.BAD_REQUEST);


    private final String message;
    private final HttpStatus httpStatus;


}
