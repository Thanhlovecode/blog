package com.example.blog.service;


import java.util.Map;

public interface ViewCounterService {
    void recordViewCounter(Long postId,String clientIp);
    void updateListPostViewCount(Map<Long, Integer> postIncrements);
}
