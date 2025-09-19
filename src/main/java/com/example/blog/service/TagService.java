package com.example.blog.service;

import com.example.blog.dto.request.TagUpdateRequest;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.TagResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TagService {
    TagResponse addTag(String name, MultipartFile file);
    PageResponse<List<TagResponse>> getAllTags(int page);
    void updateTag(String slug,TagUpdateRequest tagUpdateRequest);
}
