package com.example.blog.service.implement;

import com.example.blog.domain.Tag;
import com.example.blog.dto.request.TagUpdateRequest;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.TagResponse;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import com.example.blog.mapper.TagMapper;
import com.example.blog.repository.TagRepository;
import com.example.blog.service.CloudinaryService;
import com.example.blog.service.TagService;
import com.example.blog.utils.FileUploadUtils;
import com.example.blog.utils.PageUtils;
import com.example.blog.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "tags",allEntries = true)
    public void updateTag(String slug,TagUpdateRequest tagUpdateRequest) {

        String newSlug = SlugUtil.toSlug(tagUpdateRequest.name());

        if(tagRepository.existsBySlug(newSlug)) {
            throw new AppException(ErrorCode.TAG_ALREADY_EXISTS);
        }

        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(()-> new AppException(ErrorCode.TAG_NOT_FOUND));

        tag.setName(tagUpdateRequest.name());
        tag.setSlug(newSlug);
        tagRepository.save(tag);

    }


    @Override
    @Cacheable(cacheNames = "tags",key = "'page_'+#page",
            condition = "#page<=10")
//    @JsonCache(cacheName = "tags",timeToLive = 1800)
    public PageResponse<List<TagResponse>> getAllTags(int page) {
        Page<Tag> tags = tagRepository.findAll(PageUtils.createPageable(page));
        List<TagResponse> tagResponses = tags.getContent().stream()
                .map(tagMapper::toTagResponse)
                .toList();
        return PageResponse.fromPage(tags,tagResponses);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "tags",allEntries = true)
    @PreAuthorize("hasRole('ADMIN')")
    public TagResponse addTag(String tagName, MultipartFile multipartFile) {
        FileUploadUtils.assertAllowed(multipartFile);

        String thumbnailUrl = cloudinaryService.uploadThumbnail(multipartFile);
        Tag tag = Tag.builder()
                .name(tagName)
                .slug(SlugUtil.toSlug(tagName))
                .thumbnailUrl(thumbnailUrl)
                .build();

        tagRepository.save(tag);
        return tagMapper.toTagResponse(tag);
    }


}
