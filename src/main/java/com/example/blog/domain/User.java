package com.example.blog.domain;

import com.example.blog.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(unique = true, nullable = false,columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;


    @OneToOne(mappedBy = "user",cascade = {CascadeType.MERGE,CascadeType.PERSIST}
            ,fetch = FetchType.LAZY,orphanRemoval = true,optional = false)
    private Profile profile;
}
