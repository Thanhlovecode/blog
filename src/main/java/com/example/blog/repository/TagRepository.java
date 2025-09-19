package com.example.blog.repository;

import com.example.blog.domain.Post;
import com.example.blog.domain.Tag;
import com.example.blog.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Set<Tag> findAllByIdIn(Set<Long> ids);



}