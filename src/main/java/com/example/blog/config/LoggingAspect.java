package com.example.blog.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {
    @Before("execution(* com.example.blog.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.info("Entering method: {} belong to class: {}", joinPoint.getSignature().getName(),
                joinPoint.getTarget().getClass().getName());
    }
}
