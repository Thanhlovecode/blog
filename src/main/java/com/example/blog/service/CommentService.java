package com.example.blog.service;

import com.example.blog.dto.request.CommentRequest;
import com.example.blog.dto.request.CommentUpdateRequest;
import com.example.blog.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse saveComment(CommentRequest commentRequest);
    CommentResponse updateComment(Long commentId, CommentUpdateRequest commentRequest);
    CommentResponse deleteComment(Long commentId);
    List<CommentResponse> getTop5CommentByPostId(Long postId);
}
