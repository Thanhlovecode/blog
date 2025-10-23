package com.example.blog.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration(proxyBeanMethods = false)
public class JwtEncoderConfig {

    @Value("${security.jwt.private-key}")
    private RSAPrivateKey privateKey;

    @Value("${security.jwt.public-key}")
    private RSAPublicKey publicKey;

    @Bean
    public JwtEncoder jwtEncoder() {
        RSAKey rsa = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsa));
        return new NimbusJwtEncoder(jwkSource);
    }
}
