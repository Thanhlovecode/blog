package com.example.blog.service;

import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;

import java.util.List;

public interface PostCacheService {
    PageResponse<Long> getPostIdsPage(int page);
    List<PostResponse> getListPostResponseFromCache(List<Long> postIds);
    void multiSetPostResponses(List<PostResponse> postResponses);
    void multiSetViewCounts(List<PostResponse> postResponses);
    List<Integer> getListViewCountFromCache(List<Long> postIds);
}
