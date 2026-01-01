package com.example.blog.config;

import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.InvalidTokenException;
import com.example.blog.service.RedisService;
import com.example.blog.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.interfaces.RSAPublicKey;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    private final RedisService redisService;

    @Value("${security.jwt.public-key}")
    private RSAPublicKey publicKey;

    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt = NimbusJwtDecoder.withPublicKey(publicKey)
                .build()
                .decode(token);

        String keyTokenBlacklist = TokenUtils.AT_BLACK_LIST+ jwt.getId();
        if(StringUtils.hasLength(redisService.getString(keyTokenBlacklist))){
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN.getMessage());
        }

        long userId = Long.parseLong(jwt.getClaimAsString(TokenUtils.USER_ID));
        String accessKeyWhiteList = redisService.getString(TokenUtils.AT_WHITE_LIST+userId);
        if(!StringUtils.hasLength(accessKeyWhiteList)){
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN.getMessage());
        }

       return jwt;
    }
}
