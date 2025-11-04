package com.example.blog.annotation;


import com.example.blog.enums.KeyType;
import com.example.blog.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    RateLimitType type();
    /**
     * Key type: USER (theo userId), IP (theo IP address), GLOBAL (toàn hệ thống)
     */
    KeyType keyType() default KeyType.USER;

    String message() default "Too many requests. Please try again later.";

}