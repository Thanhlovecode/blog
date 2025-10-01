package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Builder
@Getter
@Setter
public class CommentResponse {
    private Long id;
    private String username;
    private String displayName;
    private String avatarUser;
    private String content;
    private int totalLikes;
    private LocalDateTime createdAt;
    private Long parentCommentId;
    private List<CommentResponse> replies;
}
