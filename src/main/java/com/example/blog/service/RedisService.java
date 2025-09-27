package com.example.blog.service;

public interface RedisService {
    void setString(String key, String value,long expire);
    String getString(String key);
    void deleteKey(String key);
    boolean setStringIfAbsent(String key, String value,int expire);

}
