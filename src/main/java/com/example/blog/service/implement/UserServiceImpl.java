package com.example.blog.service.implement;

import com.example.blog.domain.User;
import com.example.blog.dto.request.UserRequest;
import com.example.blog.enums.ErrorCode;
import com.example.blog.enums.UserStatus;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.MailService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    @Value("${email.subject}")
    private String subject;


    @Value("${email.content}")
    private String content;


    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MailService mailService;


    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("disable user with email: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void createUser(UserRequest request) {
        User user = userMapper.toUser(request);

        userRepository.save(user);
        log.info("User created with email: {} ", request.email());
        mailService.sendSimpleMail(user.getEmail(),subject,content);
    }

}
