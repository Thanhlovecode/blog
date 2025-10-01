package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Comment extends BaseEntity {

    @Column(nullable = false,  length = 2000)
    private String content;

    @Column(length = 100, nullable = false)
    private String username;

    @Column(length = 100, nullable = false)
    private String displayName;

    @Column(length = 300)
    private String userAvatar;

    @Column(nullable = false)
    @Builder.Default
    private int totalLikes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

}
