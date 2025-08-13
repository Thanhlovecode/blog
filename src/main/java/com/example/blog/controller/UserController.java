package com.example.blog.controller;

import com.example.blog.dto.request.UserRequest;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData createUser(@RequestBody @Valid UserRequest userRequest) {
        userService.createUser(userRequest);
        return returnData(HttpStatus.CREATED,"User Created Successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return returnData(HttpStatus.OK,"User deleted successfully");
    }

    private ResponseData returnData(HttpStatus status, String message) {
        return ResponseData.builder()
                .status(status.value())
                .message(message)
                .build();
    }



}
