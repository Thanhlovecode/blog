package com.example.blog.utils;

import com.example.blog.config.CustomAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    public static String getCurrentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static Long getCurrentUserId() {
        CustomAuthenticationToken authentication = (CustomAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getUserId();
    }

    public static String getIpAddress(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .orElse(request.getRemoteAddr());
    }
}
