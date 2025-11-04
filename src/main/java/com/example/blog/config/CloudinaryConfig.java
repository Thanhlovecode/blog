package com.example.blog.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final CloudinaryProperties cloudinaryProperties;

    private static final String CLOUD_NAME = "cloud_name";
    private static final String API_KEY = "api_key";
    private static final String API_SECRET = "api_secret";



    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                CLOUD_NAME, cloudinaryProperties.getCloudName(),
                API_KEY, cloudinaryProperties.getApiKey(),
                API_SECRET,cloudinaryProperties.getApiSecret()));
    }
}
