package com.example.blog.service.implement;

import com.example.blog.domain.Tag;
import com.example.blog.dto.request.TagUpdateRequest;
import com.example.blog.dto.response.CloudinaryResponse;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.TagResponse;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.TagMapper;
import com.example.blog.repository.TagRepository;
import com.example.blog.service.CloudinaryService;
import com.example.blog.service.TagService;
import com.example.blog.utils.FileUploadUtil;
import com.example.blog.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j(topic = "TAG-SERVICE")
public class TagServiceImpl implements TagService {


    private final TagRepository tagRepository;
    private final CloudinaryService cloudinaryService;
    private final TagMapper tagMapper;


    @Override
    @Transactional
    public void updateTag(String slug,TagUpdateRequest tagUpdateRequest) {
        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(()-> new AppException(ErrorCode.TAG_NOT_FOUND));

        tag.setName(tagUpdateRequest.name());
        tag.setSlug(SlugUtil.toSlug(tagUpdateRequest.name()));
        tagRepository.save(tag);
    }


    @Override
    @Cacheable(cacheNames = "tags",key = "'page_'+#page")
    public PageResponse<List<TagResponse>> getAllTags(int page) {
        Page<Tag> tags = tagRepository.findAll(createPageable(page));
        List<TagResponse> tagResponses = tags.getContent().stream()
                .map(tagMapper::toTagResponse)
                .toList();
        return PageResponse.fromPage(tags,tagResponses);
    }

    @Override
    @Transactional
    public String addTag(String tagName, MultipartFile multipartFile) {
        FileUploadUtil.assertAllowed(multipartFile);

        CloudinaryResponse cloudinaryResponse = cloudinaryService
                .upload(multipartFile);

        Tag tag = Tag.builder()
                .name(tagName)
                .slug(SlugUtil.toSlug(tagName))
                .thumbnailUrl(cloudinaryResponse.thumbnailUrl())
                .build();
        try{
            tagRepository.save(tag);
        } catch (DataIntegrityViolationException ex){
            throw new AppException(ErrorCode.TAG_ALREADY_EXISTS);
        }
        return cloudinaryResponse.thumbnailUrl();
    }


    private Pageable createPageable(int page) {
        return PageRequest.of(page-1, 20);
    }
}
