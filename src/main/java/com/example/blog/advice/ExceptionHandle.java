package com.example.blog.advice;

import com.example.blog.dto.response.ErrorResponse;
import com.example.blog.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;
import java.util.Objects;

@ControllerAdvice
public class ExceptionHandle {

    private static final String ERROR = "ERROR!!!";

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException exception
            , HttpServletRequest request) {
        return handleErrorResponse(exception.getMessage(), request, exception.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception
            , HttpServletRequest servletRequest) {

        String message = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();

//        var attributes = exception.getBindingResult().getAllErrors()
//                .stream()
//                .findFirst()
//                .map(objectError -> objectError.unwrap(ConstraintViolation.class))
//                .map(violation -> violation.getConstraintDescriptor().getAttributes())
//                .orElse(null);

        return handleErrorResponse(message, servletRequest, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ResponseEntity<ErrorResponse> handleErrorResponse(String message, HttpServletRequest servletRequest
            , HttpStatus status) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .code(status.value())
                        .timestamp(ZonedDateTime.now())
                        .path(servletRequest.getRequestURI())
                        .error(ERROR)
                        .message(message)
                        .build());
    }
}
