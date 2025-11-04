package com.example.blog.service.implement;

import com.example.blog.dto.response.PostResponse;
import com.example.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "REDIS-SERVICE")
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void incrementSortedSetScore(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
    }


    @Override
    public void multiIncrementKeys(Map<String, Integer> keyIncrements) {
        if (keyIncrements == null || keyIncrements.isEmpty()) return;

        long startTime = System.currentTimeMillis();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var keySerializer = redisTemplate.getStringSerializer();

            for (Map.Entry<String, Integer> entry : keyIncrements.entrySet()) {
                byte[] rawKey = keySerializer.serialize(entry.getKey());
                if (rawKey != null) {
                    connection.stringCommands().incrBy(rawKey, entry.getValue());
                }
            }
            return null;
        });

        log.info("Pipelined multiIncrement executed in {} ms ({} keys)",
                System.currentTimeMillis() - startTime, keyIncrements.size());
    }


    @Override
    public void setObject(String key, Object value, long expire) {
        if (!StringUtils.hasLength(key) || value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        log.info("Set object key: {}", key);
    }

    @Override
    public <T> void multiSetWithExpire(Map<String, T> keyValueMap, long expire) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }
        long startTime = System.currentTimeMillis();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var keySerializer = redisTemplate.getStringSerializer();
            RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();

            keyValueMap.forEach((key, value) -> {
                byte[] rawKey = keySerializer.serialize(key);
                byte[] rawValue = valueSerializer.serialize(value);
                if (rawKey != null && rawValue != null) {
                    connection.stringCommands().setEx(rawKey, expire, rawValue);
                }
            });
            return null;
        });
        log.info("Pipelined multiSet executed in {} ms", System.currentTimeMillis() - startTime);
    }

    @Override
    public <T> List<T> multiGetValues(List<String> keys, Class<T> type) {
        return redisTemplate.opsForValue().multiGet(keys)
                .stream()
                .filter(Objects::nonNull)
                .map(type::cast)
                .toList();
    }

    @Override
    public Set<ZSetOperations.TypedTuple<Object>> popMinFromSortedSet(String key, long count) {
        long size = redisTemplate.opsForZSet().zCard(key);
        if (size == 0) {
            return Collections.emptySet();
        }
        return redisTemplate.opsForZSet().popMin(key, count);
    }


    @Override
    public void setString(String key, String value, long expire) {
        if (!StringUtils.hasLength(key)) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        log.info("set key:{}", key);
    }


    @Override
    public boolean setStringIfAbsent(String key, String value, int expire) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);

    }

    @Override
    public void deleteKey(String key) {
        if (!StringUtils.hasLength(key)) {
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
