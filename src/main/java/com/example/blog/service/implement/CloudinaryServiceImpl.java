package com.example.blog.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.blog.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final String FOLDER = "folder";
    private static final String URL = "url";
    private final Cloudinary cloudinary;


    @Override
    public String upload(MultipartFile file, String folder) {
        try {
             var data = cloudinary.uploader().upload(
                    file.getBytes(), ObjectUtils.asMap(FOLDER,folder));
             log.info("Upload image to Cloudinary successfully");
             return data.get(URL).toString();
        } catch (IOException e) {
            throw new RuntimeException("Fail to upload image to Cloudinary");
        }
    }
}

