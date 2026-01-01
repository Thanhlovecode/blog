package com.example.blog.service;

import com.example.blog.dto.response.FollowResponse;
import com.example.blog.dto.response.PageResponse;

public interface FollowService {
    void followUser(Long currentUserId, Long userIdToFollow);
    void unfollowUser(Long currentUserId, Long userIdToUnfollow);
    PageResponse<FollowResponse> getFollowingByUserId(Long userId,int page);
    PageResponse<FollowResponse> getFollowerByUserId(Long userId,int page);
}
