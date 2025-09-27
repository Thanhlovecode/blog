package com.example.blog.mapper;

import com.example.blog.domain.Role;
import com.example.blog.domain.User;
import com.example.blog.dto.request.UserRequest;
import com.example.blog.enums.UserStatus;
import jakarta.mail.search.SearchTerm;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserMapper {
    public User toUser(UserRequest userRequest, Set<Role> roles) {
        return User.builder()
                .email(userRequest.email())
                .fullName(userRequest.fullName())
                .username(userRequest.username())
                .roles(roles)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
