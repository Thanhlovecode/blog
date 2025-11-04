package com.example.blog.config;


import com.example.blog.annotation.RateLimit;
import com.example.blog.dto.response.RateLimitResponse;
import com.example.blog.exception.RateLimitExceededException;
import com.example.blog.service.implement.RateLimitService;
import com.example.blog.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j(topic = "RATE-LIMIT-ASPECT")
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    @Around("@annotation(com.example.blog.annotation.RateLimit)")
    public Object rateLimitAdvice(org.aspectj.lang.ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);

        String key = resolveKey(rateLimit);

        RateLimitResponse rateLimitResponse = rateLimitService.tryConsume(key,rateLimit.type());

        if(rateLimitResponse.isAllowed()){
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit exceeded for key: {}. Retry after {} seconds", key, rateLimitResponse.getRetryAfterSeconds());
            throw new RateLimitExceededException("Rate limit exceeded. Please try again after:" + rateLimitResponse.getRetryAfterSeconds() + " seconds.");
        }
    }


    private String resolveKey(RateLimit rateLimit) {
        return switch (rateLimit.keyType()){
            case USER -> {
                String username = SecurityUtils.getCurrentUserName();
                if(username != null){
                    yield "USER:" + username;
                } else {
                    yield "IP:" + getClientIp();
                }
            }
            case IP -> "IP:" + getClientIp();
            case GLOBAL -> "GLOBAL";
        };
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return "UNKNOWN";
        HttpServletRequest request = attributes.getRequest();
        return SecurityUtils.getIpAddress(request);
    }
}
