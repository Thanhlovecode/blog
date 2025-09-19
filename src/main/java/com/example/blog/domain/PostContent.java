package com.example.blog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_content")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostContent {

    @Id
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id")
    private Post post;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

}
