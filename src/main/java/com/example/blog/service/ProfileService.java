package com.example.blog.service;

import com.example.blog.dto.request.ContactInfoRequest;
import com.example.blog.dto.request.PersonalInfoRequest;
import com.example.blog.dto.response.ContactInfoResponse;
import com.example.blog.dto.response.PersonalInfoResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    void updateContactInfo(Long id,ContactInfoRequest infoRequest);
    void updatePersonalInfo(Long id,PersonalInfoRequest infoRequest);
    PersonalInfoResponse getPersonalInfo(Long id);
    ContactInfoResponse getContactInfo(Long id);
    String uploadImageUser(MultipartFile file, Long id);

}
