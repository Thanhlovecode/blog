package com.example.blog.dto.request;

import com.example.blog.enums.Gender;

import java.time.LocalDate;


public record PersonalInfoRequest(
        String firstname,
        String lastname,
        LocalDate birthday,
        Gender gender
) {
}
