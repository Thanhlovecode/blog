package com.example.blog.config;

import com.example.blog.annotation.JsonCache;
import com.example.blog.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class JsonCacheAspect {
    private final ObjectMapper objectMapper;
    private final RedisService redisService;

    public JsonCacheAspect(ObjectMapper objectMapper, RedisService redisService) {
        this.objectMapper = objectMapper;
        this.redisService = redisService;
    }

    @Around("@annotation(jsonCache)")
    public Object around(ProceedingJoinPoint joinPoint, JsonCache jsonCache) throws Throwable {
        String cacheKey = generateCacheKey(joinPoint,jsonCache);
        String cacheValue = redisService.getString(cacheKey);

        if(cacheValue != null) {
            log.info("Cache hit for cache key:{}", cacheKey);
            return deserializeFromCache(joinPoint,cacheValue);
        }
        log.info("Cache miss for cache key:{}", cacheKey);

        Object result = joinPoint.proceed();
        if(result != null) {
            cacheResult(cacheKey,result,jsonCache.timeToLive());
            log.info("Successfully cache for cache key:{}", cacheKey);
        }
        return result;
    }

    private String generateCacheKey(ProceedingJoinPoint joinPoint,JsonCache jsonCache) {
        Object[] args = joinPoint.getArgs();
        String argsKey = Arrays.stream(args)
                    .map(Object::toString)
                    .collect(Collectors.joining("_"));
        return jsonCache.cacheName()+"::"+argsKey;

    }

    private void cacheResult(String cacheKey, Object result,int time) throws JsonProcessingException {
        String jsonValue = objectMapper.writeValueAsString(result);
        redisService.setString(cacheKey,jsonValue,time);
    }


    private Object deserializeFromCache(ProceedingJoinPoint joinPoint, String cachedValue)
            throws JsonProcessingException {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Type returnType = method.getGenericReturnType();

        // Convert Type thành JavaType để Jackson hiểu
        JavaType javaType = objectMapper.getTypeFactory().constructType(returnType);

        return objectMapper.readValue(cachedValue, javaType);
    }



}
