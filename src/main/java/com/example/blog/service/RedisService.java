package com.example.blog.service;

public interface RedisService {
    void setString(String key, String value,int expire);
    String getString(String key);
    void deleteKey(String key);

}
