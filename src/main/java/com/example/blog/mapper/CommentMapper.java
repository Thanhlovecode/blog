package com.example.blog.mapper;

import com.example.blog.domain.Comment;
import com.example.blog.dto.response.CommentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {
    public CommentResponse toCommentResponse(Comment comment) {
       return CommentResponse.builder()
               .id(comment.getId())
               .parentCommentId(comment.getParentCommentId())
               .displayName(comment.getDisplayName())
               .content(comment.getContent())
               .avatarUser(comment.getUserAvatar())
               .createdAt(comment.getCreatedAt())
               .totalLikes(comment.getTotalLikes())
               .username(comment.getUsername())
               .build();
    }

}
