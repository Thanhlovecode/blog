package com.example.blog.service;

import com.example.blog.dto.response.PostResponse;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {
    void setString(String key, String value,long expire);
    String getString(String key);
    void deleteKey(String key);
    boolean setStringIfAbsent(String key, String value,int expire);
    void incrementSortedSetScore(String key, String member,double score);
    Set<ZSetOperations.TypedTuple<Object>> popMinFromSortedSet(String key,long count);
    <T>List<T> multiGetValues(List<String> keys, Class<T> type);
    <T> void multiSetWithExpire(Map<String,T> batchMap, long expire);
    void setObject(String key, Object value,long expire);
    void multiIncrementKeys(Map<String,Integer> keyIncrements);
}
