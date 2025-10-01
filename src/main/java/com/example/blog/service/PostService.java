package com.example.blog.service;

import com.example.blog.dto.request.PostRequest;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.PostResponseDetail;
import com.example.blog.dto.request.PostStatusUpdateRequest;
import com.example.blog.dto.request.PostUpdateRequest;

import java.util.List;

public interface PostService {
    void createPost(PostRequest postRequest,String requestId);
    PostResponseDetail getPostDetailBySlug(String slug);
    void updateStatusPost(String slug, PostStatusUpdateRequest status);
    void updatePost(String slug, PostUpdateRequest updateRequest);
    PageResponse<List<PostResponse>> getPublishedPostsByUsername(String username,int page);
    PageResponse<List<PostResponse>> getPublishedPostsByTagSlug(String slug,int page,String sortBy);
    PageResponse<List<PostResponse>> getPublishedPostsByKeySearch(String keySearch,int page,String sortBy);
    PageResponse<List<PostResponse>> getNewestPublishedPost(int page);
}
