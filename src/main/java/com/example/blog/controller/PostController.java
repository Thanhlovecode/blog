package com.example.blog.controller;

import com.example.blog.annotation.RateLimit;
import com.example.blog.dto.request.PostRequest;
import com.example.blog.dto.request.PostStatusUpdateRequest;
import com.example.blog.dto.request.PostUpdateRequest;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.PostResponseDetail;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.enums.KeyType;
import com.example.blog.enums.RateLimitType;
import com.example.blog.event.PostViewEvent;
import com.example.blog.service.PostService;
import com.example.blog.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("${api.prefix}/posts")
@Slf4j
@RequiredArgsConstructor
public class PostController {

    private static final String DEFAULT_PAGE_NO = "1";
    private static final String DEFAULT_SORT_BY = "publishedAt";
    private static final String RETURN_MESSAGE_POST = "Posts retrieved successfully";

    private final PostService postService;
    private final ApplicationEventPublisher publisher;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RateLimit(
            type = RateLimitType.CREATE_POST,
            keyType = KeyType.USER
    )
    public ResponseData<Void> createPost(@Valid @RequestBody PostRequest postRequest) {
        postService.createPost(postRequest);
        return ResponseData.
                successWithMessage("Post Created Successfully", HttpStatus.CREATED);
    }

    @GetMapping("/search")
    @RateLimit(
            type = RateLimitType.SEARCH_POST,
            keyType = KeyType.IP
    )
    public ResponseData<PageResponse<PostResponse>> searchPosts(@RequestParam("keyword") @NotBlank String keyword,
                                                                      @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NO) int page,
                                                                      @RequestParam(required = false, defaultValue = DEFAULT_SORT_BY) String sortBy) {
        return ResponseData.successWithData(RETURN_MESSAGE_POST,
                postService.getPublishedPostsByKeySearch(keyword, page, sortBy),HttpStatus.OK);
    }


    @GetMapping("/user/{username}")
    public ResponseData<PageResponse<PostResponse>> getPostsByUsername(@PathVariable String username,
                                                                             @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NO) int page) {
        return ResponseData.successWithData(RETURN_MESSAGE_POST,
                postService.getPublishedPostsByUsername(username, page),HttpStatus.OK );
    }

    @GetMapping("/tags/{slug}")
    public ResponseData<PageResponse<PostResponse>> getPostsByTagSlug(@PathVariable String slug,
                                                                            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NO) int page,
                                                                            @RequestParam(required = false, defaultValue = DEFAULT_SORT_BY) String sortBy) {
        return ResponseData.successWithData(RETURN_MESSAGE_POST,
                postService.getPublishedPostsByTagSlug(slug, page, sortBy),HttpStatus.OK);
    }

    @GetMapping("/newest")
    public ResponseData<PageResponse<PostResponse>> getNewestPosts(
            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NO) int page) {
        return ResponseData.successWithData(RETURN_MESSAGE_POST,
                postService.getNewestPublishedPost(page),HttpStatus.OK);
    }

    @GetMapping("/{slug}")
    public ResponseData<PostResponseDetail> getPostDetailBySlug(@PathVariable String slug, HttpServletRequest request) {
        String clientIp = SecurityUtils.getIpAddress(request);
        PostResponseDetail postResponseDetail = postService.getPostDetailBySlug(slug, clientIp);

        publisher.publishEvent(new PostViewEvent(postResponseDetail.id(), clientIp));

        return ResponseData.successWithData(
                "Get Post by Slug Successfully", postResponseDetail,HttpStatus.OK
        );
    }


    @PatchMapping("/{slug}")
    public ResponseData<Void> updateStatusPost(@PathVariable String slug,
                                               @RequestBody @Valid PostStatusUpdateRequest status) {
        postService.updateStatusPost(slug, status);
        return ResponseData.successWithMessage("Post Updated Status Successfully",
                HttpStatus.OK);
    }

    @PutMapping("/{slug}")
    public ResponseData<Void> updatePost(@PathVariable String slug,
                                         @RequestBody @Valid PostUpdateRequest postUpdateRequest) {
        postService.updatePost(slug, postUpdateRequest);
        return ResponseData.successWithMessage("Post Updated Successfully",
                HttpStatus.OK);
    }
//
//    @GetMapping("/hello")
//    public String hello(CustomAuthenticationToken authentication){
//        System.out.println(authentication.getCredentials());
//        System.out.println(authentication.getAuthorities());
//        System.out.println(authentication.getPrincipal());
//        System.out.println(authentication.getName());
//        System.out.println(authentication.getDetails());
//        System.out.println(authentication.getUserId());
//
//        return "Hello World";
//    }
}
