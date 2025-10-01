package com.example.blog.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
public class CustomAuthenticationToken extends JwtAuthenticationToken {
    private final Long userId;
    public CustomAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities,Long userId) {
        super(jwt, authorities);
        this.userId = userId;
    }
}
