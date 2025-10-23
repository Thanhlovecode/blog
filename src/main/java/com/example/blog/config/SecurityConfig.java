package com.example.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final CustomJwtDecoder customJwtDecoder;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    private final String[] PUBLIC_ENDPOINTS = {"/api/v1/auth/**","/api/v1/users/**","/api/v1/posts/**"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(requestMatcherRegistry ->
                requestMatcherRegistry.requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .anyRequest()
                        .authenticated());
        httpSecurity.oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer -> jwtConfigurer
                                        .decoder(customJwtDecoder)
                                        .jwtAuthenticationConverter(customJwtAuthenticationConverter))
                                        .authenticationEntryPoint(authenticationEntryPoint))
                        .sessionManagement(session -> session
                                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // no save state server
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }



}
