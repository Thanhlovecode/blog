package com.example.blog.dto.response;

import com.example.blog.enums.Gender;

import java.time.LocalDate;

public record PersonalInfoResponse(String firstname, String lastname, LocalDate birthday, Gender gender) {


}
