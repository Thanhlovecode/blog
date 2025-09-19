package com.example.blog.service.implement;

import com.example.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setString(String key, String value,int expire) {
        if(!StringUtils.hasLength(key)){
            return;
        }
        redisTemplate.opsForValue().set(key, value,expire, TimeUnit.SECONDS);
        log.info("set key:{}", key);
    }


    @Override
    public boolean setStringIfAbsent(String key, String value, int expire) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);

    }

    @Override
    public void deleteKey(String key) {
        if(!StringUtils.hasLength(key)){
            return;
        }
        redisTemplate.unlink(key);
    }

    @Override
    public String getString(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                .map(String::valueOf)
                .orElse(null);
    }
}
