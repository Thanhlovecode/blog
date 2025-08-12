package com.example.blog.domain;

import com.example.blog.enums.Gender;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Profile{

    @Id
    private long id;

    private String firstName;
    private String lastName;

    private String avatar;
    private String address;

    private LocalDate birthday;
    private String phone;


    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

}
