package com.example.blog.mapper;

import com.example.blog.domain.Tag;
import com.example.blog.dto.response.TagResponse;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagResponse toTagResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getSlug(), tag.getThumbnailUrl());
    }
}

