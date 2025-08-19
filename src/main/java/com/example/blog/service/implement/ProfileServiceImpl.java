package com.example.blog.service.implement;

import com.example.blog.domain.Profile;
import com.example.blog.domain.User;
import com.example.blog.dto.request.ContactInfoRequest;
import com.example.blog.dto.request.PersonalInfoRequest;
import com.example.blog.dto.response.ContactInfoResponse;
import com.example.blog.dto.response.PersonalInfoResponse;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.ProfileMapper;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.CloudinaryService;
import com.example.blog.service.ProfileService;
import com.example.blog.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public void updateContactInfo(Long id, ContactInfoRequest infoRequest) {
        Profile profile = getProfileById(id);
        profileMapper.mapContactInfo(infoRequest, profile);
        profileRepository.save(profile);
    }

    @Override
    public PersonalInfoResponse getPersonalInfo(Long id) {


        return profileRepository.getPersonalInfo(id);
    }

    @Override
    @Transactional
    public String uploadImage(MultipartFile file, String folder, Long id) {
        FileUploadUtil.assertAllowed(file);

        String imageUrl = cloudinaryService.upload(file, folder);

        Profile profile = getProfileById(id);
        profile.setAvatar(imageUrl);
        profileRepository.save(profile);

        log.info("Avatar user with id {} has been uploaded", id);
        return imageUrl;
    }

    @Override
    public ContactInfoResponse getContactInfo(Long id) {
        return profileRepository.getContactInfo(id);
    }

    @Override
    @Transactional
    public void updatePersonalInfo(Long id, PersonalInfoRequest infoRequest) {
        Profile profile = getProfileById(id);
        profileMapper.mapPersonalInfo(infoRequest, profile);
        profileRepository.save(profile);
    }


    private Profile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseGet(()-> Profile.builder()
                        .user(getUserById(id))
                        .build()
                );
    }


    private User getUserById(Long id) {
        if(!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return userRepository.getReferenceById(id);
    }
}
