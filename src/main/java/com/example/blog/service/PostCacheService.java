package com.example.blog.service;

import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.PostResponseDetail;

import java.util.List;
import java.util.Map;

public interface PostCacheService {
    PageResponse<Long> getPostIdsPage(int page);
    List<PostResponse> getListPostResponseFromCache(List<Long> postIds);
    void multiSetPostResponses(List<PostResponse> postResponses);
    Map<Long, Long> getListViewCountFromCache(List<Long> postIds);
    void warmUpViewCounts(Map<Long, Long> postIdViewCounts);
    Long getViewCountRealTime(Long postId);
    void warmUpSingleViewCount(Long postId, long dbViewCount);
    PostResponseDetail getCachedPostContent(String slug);
}
