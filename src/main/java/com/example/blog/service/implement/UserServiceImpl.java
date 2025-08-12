package com.example.blog.service.implement;

import com.example.blog.domain.User;
import com.example.blog.dto.request.UserRequest;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public void createUser(UserRequest request) {
        User user = userMapper.toUser(request);
        try{
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        log.info("User created with email: {} ", request.email());
    }


}
