package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "Comment content must not be blank")
        @Size(max = 2000, message = "Comment content must not exceed 2000 characters")
        String content,

       @NotNull(message = "Post id must not be null")
       Long postId,

       Long parentCommentId) {
}
