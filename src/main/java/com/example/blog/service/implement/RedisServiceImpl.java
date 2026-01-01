package com.example.blog.service.implement;

import com.example.blog.dto.response.PostResponse;
import com.example.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.blog.utils.RedisKeys.POST_VIEW_HASH_KEY;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "REDIS-SERVICE")
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void incrementHash(String key, String field, long delta) {
        try {
            stringRedisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.error("Error incrementing hash key: {}, field: {} by {}. Error: {}", key, field, delta, e.getMessage());
        }

    }

    @Override
    public void addToSet(String key, String value) {
        try {
            stringRedisTemplate.opsForSet().add(key, value);
        } catch (Exception e) {
            log.error("Error adding value: {} to set key: {}. Error: {}", value, key, e.getMessage());
        }

    }


    @Override
    public List<Long> popSetMembers(String key, long count) {
        try {
            List<String> members = stringRedisTemplate
                    .opsForSet()
                    .pop(key, count);

            if (members == null || members.isEmpty()) {
                return Collections.emptyList();
            }

            return members.stream()
                    .map(this::safeParseToLong)
                    .filter(Objects::nonNull)
                    .collect(Collectors. toList());

        } catch (Exception e) {
            log.error("Error popping set members for key: {}", key, e);
            return Collections.emptyList();
        }
    }

    private Long safeParseToLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error("Cannot parse to Long: {}", value);
            return null;
        }
    }


    @Override
    public Long getViewCountRealTime(Long postId) {
        Object viewCount = redisTemplate.opsForHash().get(POST_VIEW_HASH_KEY, postId.toString());
        return viewCount != null ? Long.parseLong(viewCount.toString()) : 0L;
    }

    @Override
    public Map<Long, Long> getHashMultiGet(String key, Collection<Long> hashKeys) {
        if (hashKeys == null || hashKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<Object> stringFields = hashKeys.stream()
                    .map(String::valueOf)
                    .collect(Collectors. toList());

            List<Object> values = stringRedisTemplate.opsForHash().multiGet(key, stringFields);

            Map<Long, Long> resultMap = new HashMap<>();
            int index = 0;

            for (Long hashKey : hashKeys) {
                Object value = values.get(index++);

                if (value != null) {
                    try {
                        resultMap.put(hashKey, Long.parseLong(value.toString()));
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse to Long for field {}: {}", hashKey, value);
                    }
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("Error multi-getting hash for key: {}", key, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void hashMultiSet(String key, Map<String, String> hashKeyValues) {
        if (hashKeyValues == null || hashKeyValues.isEmpty()) {
            return;
        }
        try {
            stringRedisTemplate. opsForHash().putAll(key, hashKeyValues);
            log.debug("HMSET {} fields to key: {}", hashKeyValues.size(), key);
        } catch (Exception e) {
            log.error("Error setting multiple hash fields for key: {}", key, e);
            throw e;
        }
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

    //    @Override
//    public Set<ZSetOperations.TypedTuple<Object>> popMinFromSortedSet(String key, long count) {
//        long size = redisTemplate.opsForZSet().zCard(key);
//        if (size == 0) {
//            return Collections.emptySet();
//        }
//        return redisTemplate.opsForZSet().popMin(key, count);
//    }
    //    @Override
//    public void incrementSortedSetScore(String key, String member, double score) {
//        redisTemplate.opsForZSet().incrementScore(key, member, score);
//    }
//
//
//    @Override
//    public void multiIncrementKeys(Map<String, Integer> keyIncrements) {
//        if (keyIncrements == null || keyIncrements.isEmpty()) return;
//
//        long startTime = System.currentTimeMillis();
//
//        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
//            var keySerializer = redisTemplate.getStringSerializer();
//
//            for (Map.Entry<String, Integer> entry : keyIncrements.entrySet()) {
//                byte[] rawKey = keySerializer.serialize(entry.getKey());
//                if (rawKey != null) {
//                    connection.stringCommands().incrBy(rawKey, entry.getValue());
//                }
//            }
//            return null;
//        });
//
//        log.info("Pipelined multiIncrement executed in {} ms ({} keys)",
//                System.currentTimeMillis() - startTime, keyIncrements.size());
//    }
}
