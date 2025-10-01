package com.example.blog.controller;


import com.example.blog.dto.request.CommentRequest;
import com.example.blog.dto.request.CommentUpdateRequest;
import com.example.blog.dto.response.CommentResponse;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/comments")
@Slf4j
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<CommentResponse> createComment(@RequestBody CommentRequest commentRequest) {
        return ResponseData.successWithData(
                "Create comment successfully",
                commentService.saveComment(commentRequest),
                HttpStatus.CREATED);
    }

    @PutMapping("{commentId}")
    public ResponseData<CommentResponse> updateComment(@PathVariable Long commentId,
                                                       @RequestBody CommentUpdateRequest commentRequest) {
        return ResponseData.successWithData(
                "Update comment successfully",
                commentService.updateComment(commentId,commentRequest),
                HttpStatus.OK
        );
    }

    @DeleteMapping("{commentId}")
    public ResponseData<CommentResponse> updateComment(@PathVariable Long commentId) {
        return ResponseData.successWithData(
                "Deleted comment successfully",
                commentService.deleteComment(commentId),
                HttpStatus.OK
        );
    }
}
