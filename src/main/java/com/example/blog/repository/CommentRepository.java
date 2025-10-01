package com.example.blog.repository;

import com.example.blog.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {


    @Query("""
           select c from Comment c
           where c.post.id = :postId and c.parentCommentId is null
           """)

    List<Comment> findTop5CommentsByPostIdAndParentCommentIdIsNull(Long postId);
    List<Comment> findCommentsByParentCommentIdIn(List<Long> parentCommentIds);
}