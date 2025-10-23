package com.example.blog.service;

import com.example.blog.dto.request.PostRequest;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.PostResponseDetail;
import com.example.blog.dto.request.PostStatusUpdateRequest;
import com.example.blog.dto.request.PostUpdateRequest;

public interface PostService {
    void createPost(PostRequest postRequest);
    PostResponseDetail getPostDetailBySlug(String slug);
    void updateStatusPost(String slug, PostStatusUpdateRequest status);
    void updatePost(String slug, PostUpdateRequest updateRequest);
    PageResponse<PostResponse> getPublishedPostsByUsername(String username,int page);
    PageResponse<PostResponse> getPublishedPostsByTagSlug(String slug,int page,String sortBy);
    PageResponse<PostResponse> getPublishedPostsByKeySearch(String keySearch,int page,String sortBy);
    PageResponse<PostResponse> getNewestPublishedPost(int page);
}
