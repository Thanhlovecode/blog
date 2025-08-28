package com.example.blog.mapper;

import com.example.blog.domain.Profile;
import com.example.blog.dto.request.ContactInfoRequest;
import com.example.blog.dto.request.PersonalInfoRequest;
import com.example.blog.dto.response.CloudinaryResponse;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {
    public void mapPersonalInfo(PersonalInfoRequest personalInfoRequest,Profile profile) {
       profile.setFirstName(personalInfoRequest.firstname());
       profile.setLastName(personalInfoRequest.lastname());
       profile.setBirthday(personalInfoRequest.birthday());
       profile.setGender(personalInfoRequest.gender());
    }

    public void mapContactInfo(ContactInfoRequest contactInfoRequest,Profile profile) {
        profile.setPhone(contactInfoRequest.phone());
        profile.setAddress(contactInfoRequest.address());
    }

    public void mapImagedInfoProfile(Profile profile, CloudinaryResponse cloudinaryResponse) {
        profile.setImageUrl(cloudinaryResponse.imageUrl());
        profile.setImageId(cloudinaryResponse.publicId());
        profile.setThumbnailUrl(cloudinaryResponse.thumbnailUrl());
    }
}
