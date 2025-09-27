package com.example.blog.service;

import com.example.blog.domain.User;
import com.example.blog.enums.TypeToken;
import org.springframework.security.oauth2.jwt.Jwt;

public interface TokenService {
    String createAccessToken(User user, String tokenId, TypeToken type);
    String createRefreshToken(User user, String tokenId, TypeToken type);
    Jwt validateToken(String token);
}
