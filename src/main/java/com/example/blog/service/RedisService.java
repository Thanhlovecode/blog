package com.example.blog.service;

import com.example.blog.dto.response.PostResponse;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {
    void setString(String key, String value,long expire);
    String getString(String key);
    void deleteKey(String key);
    boolean setStringIfAbsent(String key, String value,int expire);
    <T>List<T> multiGetValues(List<String> keys, Class<T> type);
    <T> void multiSetWithExpire(Map<String,T> batchMap, long expire);
    void setObject(String key, Object value,long expire);
    void incrementHash(String key, String field, long delta);
    void addToSet(String key, String value);
    List<Long> popSetMembers(String key, long count);
    void hashMultiSet(String key, Map<String,String> hashKeyValues);
    Map<Long,Long> getHashMultiGet(String key, Collection<Long> hashKeys);
    Long getViewCountRealTime(Long postId);
}
