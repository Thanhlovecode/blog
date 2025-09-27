package com.example.blog.domain;

import com.example.blog.enums.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String excerpt;

    @Column(length = 100)
    private String username;

    @Column(length = 100,nullable = false)
    private String displayName;

    private int readingTime;

    @Column(length = 300)
    private String thumbnailUrl;

    @Column(nullable = false)
    @Builder.Default
    private int totalComments = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalViews = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalLikes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @OneToOne(mappedBy = "post", cascade = {CascadeType.MERGE, CascadeType.PERSIST}
            , fetch = FetchType.LAZY, orphanRemoval = true, optional = false)
    private PostContent postContent;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime publishedAt;

}
