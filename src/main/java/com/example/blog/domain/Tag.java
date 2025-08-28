package com.example.blog.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag extends BaseEntity{

    @Column(nullable = false)
    private String name;

    private String thumbnailUrl;


    @Column(nullable = false,unique = true)
    private String slug;

}
