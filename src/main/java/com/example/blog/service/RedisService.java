package com.example.blog.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RedisService {
    void setString(String key, String value, long expire);
    String getString(String key);
    void deleteKey(String key);
    boolean setStringIfAbsent(String key, String value, int expire);
    void setStringWithTTL(String key, String value, long ttlSeconds);

    // Pipeline: INCR viewKey + SADD dirtySetKey postId in 1 roundtrip
    void incrementAndMarkDirty(String viewKey, String dirtySetKey, String postIdStr);

    // MGET for view count String keys → Map<postId, viewCount>
    Map<Long, Long> multiGetViewCounts(List<String> viewKeys, List<Long> postIds);

    void addToSet(String key, String value);
    List<Long> popSetMembers(String key, long count);

    // Post metadata: explicit JSON serialization (no reflection / no @class)
    void setPostMeta(String key, Object value, long expireSeconds);
    <T> T getPostMeta(String key, Class<T> type);
    <T> List<T> multiGetPostMeta(List<String> keys, Class<T> type);
    <T> void multiSetPostMetaWithExpire(Map<String, T> keyValueMap, long expireSeconds, Class<T> type);

    // Warm-up: pipeline SET view_count keys with TTL
    void pipelineSetViewCounts(Map<String, String> keyValueMap, long ttlSeconds);
}
