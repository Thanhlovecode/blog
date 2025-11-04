package com.example.blog.controller;

import com.example.blog.annotation.RateLimit;
import com.example.blog.dto.request.AuthenticationRequest;
import com.example.blog.dto.request.RefreshTokenRequest;
import com.example.blog.dto.response.AuthenticationResponse;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.enums.KeyType;
import com.example.blog.enums.RateLimitType;
import com.example.blog.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("${api.prefix}/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/log-in")
    @RateLimit(
            type = RateLimitType.LOGIN,
            keyType = KeyType.IP
    )
    public ResponseData<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseData.successWithData("Log in successfully",
                authenticationService.authenticate(authenticationRequest),HttpStatus.OK);
    }

    @PostMapping("/log-out")
    public ResponseData<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader){
        authenticationService.logout(authHeader);
       return ResponseData.successWithMessage("Log-out successfully", HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseData<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseData.successWithData("Refresh Token successfully",
                authenticationService.refreshToken(refreshTokenRequest),HttpStatus.OK );
    }


}
