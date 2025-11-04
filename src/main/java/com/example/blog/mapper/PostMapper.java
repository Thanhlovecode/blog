package com.example.blog.mapper;


import com.example.blog.domain.Post;
import com.example.blog.domain.Tag;
import com.example.blog.dto.response.CommentResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.PostResponseDetail;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class PostMapper {
    public PostResponseDetail toPostResponseDetail(Post post, List<CommentResponse> comments) {
        return new PostResponseDetail(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getUsername(),
                post.getThumbnailUrl(),
                post.getDisplayName(),
                post.getReadingTime(),
                post.getTotalViews(),
                post.getTotalComments(),
                post.getTotalLikes(),
                post.getPostContent().getContent(),
                post.getPublishedAt(),
                comments
        );
    }


    public PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .excerpt(post.getExcerpt())
                .username(post.getUsername())
                .displayName(post.getDisplayName())
                .readingTime(post.getReadingTime())
                .thumbnailUrl(post.getThumbnailUrl())
                .totalComments(post.getTotalComments())
                .totalViews(post.getTotalViews())
                .status(post.getStatus())
                .publishedAt(post.getPublishedAt())
                .tags(post.getTags()
                        .stream()
                        .map(Tag::getName)
                        .collect(Collectors.toUnmodifiableSet()))
                .build();
    }

}
