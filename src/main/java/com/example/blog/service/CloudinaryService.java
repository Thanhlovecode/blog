package com.example.blog.service;

import com.example.blog.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    CloudinaryResponse upload(MultipartFile file);
    void deleteImage(String assetImageId);

}
