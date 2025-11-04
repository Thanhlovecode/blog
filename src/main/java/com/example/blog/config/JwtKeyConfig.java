package com.example.blog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
public class JwtKeyConfig {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private long accessDuration;
    private long refreshDuration;
}
