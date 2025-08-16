package com.example.blog.controller;

import com.example.blog.dto.request.ContactInfoRequest;
import com.example.blog.dto.request.PersonalInfoRequest;
import com.example.blog.dto.response.ContactInfoResponse;
import com.example.blog.dto.response.PersonalInfoResponse;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/contact/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<?> updateContactInfo(@PathVariable Long id,
                                          @RequestBody ContactInfoRequest contactRequest) {
        profileService.updateContactInfo(id,contactRequest);
        return returnData("Contact information updated successfully");
    }

    @PutMapping("/personal/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<?> updatePersonalInfo(@PathVariable Long id,
                                           @RequestBody PersonalInfoRequest personalRequest) {
        profileService.updatePersonalInfo(id,personalRequest);
        return returnData("Personal information updated successfully");
    }

    private ResponseData<?> returnData(String message) {
        return ResponseData.builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .build();
    }


    @GetMapping("/personal/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<PersonalInfoResponse> getPersonalInfo(@PathVariable Long id) {
        PersonalInfoResponse infoResponse = profileService.getPersonalInfo(id);
        return ResponseData.<PersonalInfoResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Get personal information successfully")
                .data(infoResponse)
                .build();
    }

    @GetMapping("/contact/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<ContactInfoResponse> getContactInfo(@PathVariable Long id) {
        ContactInfoResponse contactResponse = profileService.getContactInfo(id);
        return ResponseData.<ContactInfoResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Get contact information successfully")
                .data(contactResponse)
                .build();
    }

}
