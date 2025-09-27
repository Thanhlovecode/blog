package com.example.blog.service.implement;

import com.example.blog.domain.Profile;
import com.example.blog.domain.User;
import com.example.blog.dto.request.ContactInfoRequest;
import com.example.blog.dto.request.PersonalInfoRequest;
import com.example.blog.dto.response.CloudinaryResponse;
import com.example.blog.dto.response.ContactInfoResponse;
import com.example.blog.dto.response.PersonalInfoResponse;
import com.example.blog.enums.ErrorCode;
import com.example.blog.event.ImageCleanUpEvent;
import com.example.blog.event.ProfileImageUpdateEvent;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.ProfileMapper;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.CloudinaryService;
import com.example.blog.service.ProfileService;
import com.example.blog.utils.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private final ApplicationEventPublisher publisher;

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
    public String uploadImageUser(MultipartFile file, Long userId) {
        FileUploadUtils.assertAllowed(file);

        Profile profile = getProfileById(userId);

        if (StringUtils.hasLength(profile.getImageId())) {
            publisher.publishEvent(new ImageCleanUpEvent(this, profile.getImageId()));
        }

        CloudinaryResponse cloudinaryResponse = cloudinaryService.uploadImage(file);

        profileMapper.mapImagedInfoProfile(profile, cloudinaryResponse);
        profileRepository.save(profile);


        log.info("Avatar user with userId {} has been uploaded", userId);
        publisher.publishEvent(new ProfileImageUpdateEvent(this,cloudinaryResponse.thumbnailUrl(),userId));
        return cloudinaryResponse.thumbnailUrl();
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
                .orElseGet(() -> Profile.builder()
                        .user(getUserById(id))
                        .build()
                );
    }


    private User getUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return userRepository.getReferenceById(id);
    }
}
