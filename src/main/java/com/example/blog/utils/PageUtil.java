package com.example.blog.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QPageRequest;

public class PageUtil {
    public static final int PAGE_SIZE = 20;
    public static Pageable createPageable(int page) {
        return PageRequest.of(page-1, PAGE_SIZE);
    }

    public static Pageable createSortPageable(int page,String sortField) {
        return PageRequest.of(page-1,PAGE_SIZE, Sort.by(Sort.Direction.DESC,sortField));
    }
}
