package com.example.blog.service.implement;

import com.example.blog.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "REDIS-SERVICE")
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // ──────────────────────────────────────────────
    // Basic String Operations
    // ──────────────────────────────────────────────

    @Override
    public void setString(String key, String value, long expire) {
        if (!StringUtils.hasLength(key)) {
            return;
        }
        stringRedisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
        log.info("Set key: {}", key);
    }

    @Override
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteKey(String key) {
        if (!StringUtils.hasLength(key)) {
            return;
        }
        stringRedisTemplate.unlink(key);
    }

    @Override
    public boolean setStringIfAbsent(String key, String value, int expire) {
        Boolean result = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, value, expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void setStringWithTTL(String key, String value, long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    // ──────────────────────────────────────────────
    // View Counter: Pipeline INCR + SADD (1 roundtrip)
    // ──────────────────────────────────────────────

    @Override
    public void incrementAndMarkDirty(String viewKey, String dirtySetKey, String postIdStr) {
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var serializer = stringRedisTemplate.getStringSerializer();

            byte[] rawViewKey = serializer.serialize(viewKey);
            byte[] rawDirtyKey = serializer.serialize(dirtySetKey);
            byte[] rawPostId = serializer.serialize(postIdStr);

            if (rawViewKey != null) {
                connection.stringCommands().incr(rawViewKey);
            }
            if (rawDirtyKey != null && rawPostId != null) {
                connection.setCommands().sAdd(rawDirtyKey, rawPostId);
            }
            return null;
        });
    }

    // ──────────────────────────────────────────────
    // View Counter: MGET String keys
    // ──────────────────────────────────────────────

    @Override
    public Map<Long, Long> multiGetViewCounts(List<String> viewKeys, List<Long> postIds) {
        if (viewKeys == null || viewKeys.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<String> values = stringRedisTemplate.opsForValue().multiGet(viewKeys);

            if (values == null) {
                return Collections.emptyMap();
            }

            Map<Long, Long> resultMap = new HashMap<>();
            for (int i = 0; i < postIds.size(); i++) {
                String value = values.get(i);
                if (value != null) {
                    try {
                        resultMap.put(postIds.get(i), Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse view count for postId {}: {}", postIds.get(i), value);
                    }
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("Error multi-getting view counts", e);
            return Collections.emptyMap();
        }
    }

    // ──────────────────────────────────────────────
    // Set Operations (dirty_posts)
    // ──────────────────────────────────────────────

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
            List<String> members = stringRedisTemplate.opsForSet().pop(key, count);

            if (members == null || members.isEmpty()) {
                return Collections.emptyList();
            }

            return members.stream()
                    .map(this::safeParseToLong)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("Error popping set members for key: {}", key, e);
            return Collections.emptyList();
        }
    }

    // ──────────────────────────────────────────────
    // Post Metadata: Explicit JSON (NO reflection / NO @class)
    // Uses ObjectMapper.writeValueAsString / readValue directly
    // ──────────────────────────────────────────────

    @Override
    public void setPostMeta(String key, Object value, long expireSeconds) {
        if (!StringUtils.hasLength(key) || value == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, expireSeconds, TimeUnit.SECONDS);
            log.info("Set post meta key: {}", key);
        } catch (JsonProcessingException e) {
            log.error("Error serializing post meta for key: {}", key, e);
        }
    }

    @Override
    public <T> T getPostMeta(String key, Class<T> type) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing post meta for key: {}", key, e);
            return null;
        }
    }

    @Override
    public <T> List<T> multiGetPostMeta(List<String> keys, Class<T> type) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> jsonValues = stringRedisTemplate.opsForValue().multiGet(keys);

        if (jsonValues == null) {
            return Collections.emptyList();
        }

        List<T> results = new ArrayList<>();
        for (String json : jsonValues) {
            if (json != null) {
                try {
                    results.add(objectMapper.readValue(json, type));
                } catch (JsonProcessingException e) {
                    log.warn("Error deserializing cached value: {}", e.getMessage());
                }
            }
        }
        return results;
    }

    @Override
    public <T> void multiSetPostMetaWithExpire(Map<String, T> keyValueMap, long expireSeconds, Class<T> type) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }
        long startTime = System.currentTimeMillis();

        // Pre-serialize JSON OUTSIDE pipeline — avoid blocking Redis connection during CPU-bound work
        var keySerializer = stringRedisTemplate.getStringSerializer();
        Map<byte[], byte[]> serializedEntries = new LinkedHashMap<>();

        keyValueMap.forEach((key, value) -> {
            try {
                byte[] rawKey = keySerializer.serialize(key);
                String json = objectMapper.writeValueAsString(value);
                byte[] rawValue = keySerializer.serialize(json);
                if (rawKey != null && rawValue != null) {
                    serializedEntries.put(rawKey, rawValue);
                }
            } catch (JsonProcessingException e) {
                log.warn("Error serializing value for key: {}", key, e);
            }
        });

        if (serializedEntries.isEmpty()) {
            return;
        }

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            serializedEntries.forEach((rawKey, rawValue) ->
                    connection.stringCommands().setEx(rawKey, expireSeconds, rawValue));
            return null;
        });

        log.info("Pipelined multiSet metadata executed in {} ms ({} keys)",
                System.currentTimeMillis() - startTime, serializedEntries.size());
    }

    // ──────────────────────────────────────────────
    // Pipeline SET view count keys with TTL (warm-up)
    // ──────────────────────────────────────────────

    @Override
    public void pipelineSetViewCounts(Map<String, String> keyValueMap, long ttlSeconds) {
        if (keyValueMap == null || keyValueMap.isEmpty()) {
            return;
        }

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var serializer = stringRedisTemplate.getStringSerializer();

            keyValueMap.forEach((key, value) -> {
                byte[] rawKey = serializer.serialize(key);
                byte[] rawValue = serializer.serialize(value);
                if (rawKey != null && rawValue != null) {
                    connection.stringCommands().setEx(rawKey, ttlSeconds, rawValue);
                }
            });
            return null;
        });

        log.debug("Pipelined warm-up {} view count keys", keyValueMap.size());
    }

    // ──────────────────────────────────────────────
    // Private Helpers
    // ──────────────────────────────────────────────

    private Long safeParseToLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.error("Cannot parse to Long: {}", value);
            return null;
        }
    }
}
