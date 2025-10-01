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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/contact/{id}")
    public ResponseEntity<ResponseData<Void>> updateContactInfo(@PathVariable Long id,
                                               @RequestBody ContactInfoRequest contactRequest) {
        profileService.updateContactInfo(id,contactRequest);
        return ResponseEntity.ok(ResponseData.successWithMessage
                ("update contact information successfully", HttpStatus.OK)
        );
    }

    @PutMapping("/personal/{id}")

    public ResponseEntity<ResponseData<Void>>  updatePersonalInfo(@PathVariable Long id,
                                           @RequestBody PersonalInfoRequest personalRequest) {
        profileService.updatePersonalInfo(id,personalRequest);
        return ResponseEntity.ok(ResponseData.successWithMessage
                ("update personal information successfully", HttpStatus.OK)
        );
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<ResponseData<PersonalInfoResponse>> getPersonalInfo(@PathVariable Long id) {
        PersonalInfoResponse infoResponse = profileService.getPersonalInfo(id);

        return ResponseEntity.ok(ResponseData.successWithData(
                "Get personal info successfully",infoResponse,HttpStatus.OK
        ));
    }

    @GetMapping("/contact/{id}")
    public  ResponseEntity<ResponseData<ContactInfoResponse>> getContactInfo(@PathVariable Long id) {
        ContactInfoResponse contactResponse = profileService.getContactInfo(id);
        return ResponseEntity.ok(ResponseData.successWithData(
                "Get contact info successfully",contactResponse,HttpStatus.OK
        ));
    }

    @PatchMapping("/upload/image/{id}")
    public ResponseEntity<ResponseData<String>>  uploadImage(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long id) {

        String url = profileService.uploadImageUser(file,id);
        return ResponseEntity.ok(ResponseData.successWithData(
                "Upload image successfully",url,HttpStatus.OK
        ));
    }




}
