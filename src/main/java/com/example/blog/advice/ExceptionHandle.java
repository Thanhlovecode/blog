package com.example.blog.advice;

import com.example.blog.dto.response.ErrorResponse;
import com.example.blog.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.Objects;

@ControllerAdvice
public class ExceptionHandle {


    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException exception
            , HttpServletRequest request) {
        return handleErrorResponse(exception.getMessage(), request, exception.getStatus());
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleAppException(DataIntegrityViolationException exception
            , HttpServletRequest request) {
        return handleErrorResponse(exception.getMessage(), request, HttpStatus.CONFLICT);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception
            , HttpServletRequest servletRequest) {

        String message = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

        return handleErrorResponse(message, servletRequest, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ResponseEntity<ErrorResponse> handleErrorResponse(String message, HttpServletRequest servletRequest
            , HttpStatus status) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .code(status.value())
                        .timestamp(ZonedDateTime.now())
                        .path(servletRequest.getRequestURI())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .build());
    }
}
