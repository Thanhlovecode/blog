package com.example.blog.mapper;

import com.example.blog.domain.User;
import com.example.blog.dto.request.UserRequest;
import com.example.blog.enums.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toUser(UserRequest userRequest) {
        return User.builder()
                .email(userRequest.email())
                .password(userRequest.password())
                .status(UserStatus.ACTIVE)
                .build();
    }
}
