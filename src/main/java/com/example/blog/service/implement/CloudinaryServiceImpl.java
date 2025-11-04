package com.example.blog.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.blog.dto.response.CloudinaryResponse;
import com.example.blog.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CLOUDINARY-SERVICE")
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final String FOLDER = "folder";
    private static final String SECURE_URL = "secure_url";
    private static final String PUBLIC_ID = "public_id";
    private final Cloudinary cloudinary;


    @Override
    @Async
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
            log.info("Delete imaged successfully {} ", publicId);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public String uploadThumbnail(MultipartFile file) {
        Map params = ObjectUtils.asMap(
                FOLDER,"images/thumbnails",
                "quality", "auto",
                "fetch_format", "auto",
                "transformation", ObjectUtils.asMap(
                        "width", 100,
                        "height", 100,
                        "crop", "fill",
                        "gravity", "auto"
                )
        );
        try {
            var data = cloudinary.uploader().upload(file.getBytes(),params);
            log.info("Upload thumbnail successfully to cloudinary {} ", data.get(PUBLIC_ID));
            return data.get(SECURE_URL).toString();
        } catch (IOException e) {
            log.error("Failed to upload thumbnail to cloudinary", e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public CloudinaryResponse uploadImage(MultipartFile file) {
        try {
             var data = cloudinary.uploader().upload(
                    file.getBytes(), ObjectUtils.asMap(FOLDER,"images"));
             log.info("Upload image to Cloudinary successfully");

             String secureUrl = data.get(SECURE_URL).toString();
             String publicId = data.get(PUBLIC_ID).toString();
             String thumbnailUrl = createThumbnailUrl(publicId);
             return new CloudinaryResponse(secureUrl, thumbnailUrl, publicId);

        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String createThumbnailUrl(String publicId) {
        return cloudinary.url().transformation(new Transformation()
                .width(100).height(100)
                .quality("auto").gravity("auto")
                .crop("fill").fetchFormat("auto"))
                .generate(publicId);
    }
}

