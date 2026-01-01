package com.example.blog.service;


import java.util.Map;

public interface ViewCounterService {
    void recordViewCounter(Long postId,String clientIp,String userAgent);
    void updateListPostViewCount(Map<Long, Long> postIncrements);
}
