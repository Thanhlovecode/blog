package com.example.blog.service;

import com.example.blog.dto.response.CloudinaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    CloudinaryResponse uploadImage(MultipartFile file);
    void deleteImage(String assetImageId);
    String uploadThumbnail(MultipartFile file);

}
