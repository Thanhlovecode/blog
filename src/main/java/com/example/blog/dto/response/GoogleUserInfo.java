package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GoogleUserInfo {
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String picture;
    private String googleId;
    private Boolean emailVerified;
    private String locale;
}
