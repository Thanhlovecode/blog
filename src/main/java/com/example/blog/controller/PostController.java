package com.example.blog.controller;

import com.example.blog.dto.request.PostRequest;
import com.example.blog.dto.response.*;
import com.example.blog.dto.request.PostStatusUpdateRequest;
import com.example.blog.dto.request.PostUpdateRequest;
import com.example.blog.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/posts")
@Slf4j
@RequiredArgsConstructor
public class PostController {

    private static final String DEFAULT_PAGE_NO = "1";
    private static final String DEFAULT_SORT_BY = "publishedAt";
    private static final String RETURN_MESSAGE_POST = "Posts retrieved successfully";

    private final PostService postService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<Void> createPost(@Valid @RequestBody PostRequest postRequest,
                                         @RequestHeader("X-Request-Id") String requestId) {
        postService.createPost(postRequest, requestId);
        return ResponseData.
                successWithMessage("Post Created Successfully", HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseData<PageResponse<List<PostResponse>>> searchPosts(@RequestParam("keyword") @NotBlank String keyword,
                                                                      @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NO) int page,
                                                                      @RequestParam(required = false, defaultValue = DEFAULT_SORT_BY) String sortBy) {
        return ResponseData.successWithData(RETURN_MESSAGE_POST,
                postService.getPublishedPostsByKeySearch(keyword, page, sortBy));
    }


    @GetMapping("/user/{userId}")
    public ResponseData<PageResponse<List<PostResponse>>> getPostsByUserId(@PathVariable Long userId,
                                                                           @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NO) int page) {
        return ResponseData.successWithData(RETURN_MESSAGE_POST,
                postService.getPublishedPostsByUserId(userId, page));
    }

    @GetMapping("/tags/{slug}")
    public ResponseData<PageResponse<List<PostResponse>>> getPostsByTagSlug(@PathVariable String slug,
                                                                            @RequestParam(required = false, defaultValue = DEFAULT_PAGE_NO) int page,
                                                                            @RequestParam(required = false, defaultValue = DEFAULT_SORT_BY) String sortBy) {
        return ResponseData.successWithData(RETURN_MESSAGE_POST,
                postService.getPublishedPostsByTagSlug(slug, page, sortBy));
    }

    @GetMapping("/{slug}")
    public ResponseData<PostResponseDetail> getPostBySlug(
            @PathVariable String slug) {
        PostResponseDetail postResponseDetail = postService.getPostBySlug(slug);
        return ResponseData.successWithData(
                "Get Post by Slug Successfully", postResponseDetail
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

}
