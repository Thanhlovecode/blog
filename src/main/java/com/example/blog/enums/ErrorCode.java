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
    EMAIL_NOT_VERIFY_OTP("Email not verify otp", HttpStatus.BAD_REQUEST),


    FILE_IS_EMPTY("File is empty", HttpStatus.BAD_REQUEST),
    FILE_SIZE_TOO_LARGE("File is too large", HttpStatus.UNPROCESSABLE_ENTITY),
    FILE_EXTENSION_NOT_SUPPORTED("File extension not supported", HttpStatus.UNPROCESSABLE_ENTITY),


    TAG_NOT_FOUND("Tag does not exist", HttpStatus.NOT_FOUND),
    TAG_ALREADY_EXISTS("Tag already exists", HttpStatus.CONFLICT),

    POST_NOT_FOUND("Post not found", HttpStatus.NOT_FOUND),

    UNAUTHENTICATED("Unauthenticated", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("You don't have permission", HttpStatus.FORBIDDEN),
    INVALID_TOKEN("Invalid token", HttpStatus.UNAUTHORIZED),

    COMMENT_NOT_FOUND("Comment not found", HttpStatus.NOT_FOUND);



    private final String message;
    private final HttpStatus httpStatus;


}
