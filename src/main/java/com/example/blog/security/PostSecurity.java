package com.example.blog.security;

import com.example.blog.repository.PostRepository;
import com.example.blog.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostSecurity {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public boolean isPostOwner(String slug) {
        String currentUserName = SecurityUtils.getCurrentUserName();
        return postRepository.existsBySlugAndUsername(slug, currentUserName);
    }
}
