package com.example.blog.service;

import com.example.blog.dto.request.ContactInfoRequest;
import com.example.blog.dto.request.PersonalInfoRequest;
import com.example.blog.dto.response.ContactInfoResponse;
import com.example.blog.dto.response.PersonalInfoResponse;

public interface ProfileService {
    void updateContactInfo(Long id,ContactInfoRequest infoRequest);
    void updatePersonalInfo(Long id,PersonalInfoRequest infoRequest);
    PersonalInfoResponse getPersonalInfo(Long id);
    ContactInfoResponse getContactInfo(Long id);
}
