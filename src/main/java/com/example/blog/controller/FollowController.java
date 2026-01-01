package com.example.blog.controller;


import com.example.blog.dto.response.FollowResponse;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.service.FollowService;
import com.example.blog.utils.SecurityUtils;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/follows")
@Slf4j
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}")
    public ResponseData<Void> followUser(@PathVariable("userId") Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        followService.followUser(currentUserId,userId);
        return ResponseData.successWithMessage(
                "Follow successfully", HttpStatus.CREATED
        );
    }


    @GetMapping("/following")
    public ResponseData<PageResponse<FollowResponse>> getFollowing(
            @RequestParam(required = false, defaultValue = "0") @Min(0) int page) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        PageResponse<FollowResponse> followings = followService.getFollowingByUserId(currentUserId, page);
        return ResponseData.successWithData("Successfully",
                followings,
                HttpStatus.OK);
    }


    @GetMapping("/followers")
    public ResponseData<PageResponse<FollowResponse>> getFollowers(
            @RequestParam(required = false, defaultValue = "0") @Min(0) int page) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        PageResponse<FollowResponse> followers = followService.getFollowerByUserId(currentUserId, page);
        return ResponseData.successWithData("Successfully",
                followers,
                HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollowUser(@PathVariable("userId") Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        followService.unfollowUser(currentUserId,userId);
        return ResponseEntity.noContent().build();
    }
}
