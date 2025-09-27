package com.example.blog.service;

import com.example.blog.dto.request.AuthenticationRequest;
import com.example.blog.dto.request.RefreshTokenRequest;
import com.example.blog.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    void logout(String authHeader);
    AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);


}
