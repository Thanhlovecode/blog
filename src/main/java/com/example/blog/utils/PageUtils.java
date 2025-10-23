package com.example.blog.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtils {
    private PageUtils() {}

    public static final int PAGE_SIZE = 20;
    public static final String FIELD_ID = "id";
    public static final String FIELD_PUBLISHED = "publishedAt";
    public static Pageable defaultSortPageable(int page) {
        return createPageable(page, FIELD_PUBLISHED, FIELD_ID);
    }

    public static Pageable sortByFieldPageable(int page, String sortField) {
        return createPageable(page, sortField, FIELD_ID);
    }

    public static Pageable defaultNoSortPageable(int page) {
        return PageRequest.of(Math.max(0,page-1), PAGE_SIZE);
    }


    private static Pageable createPageable(int page, String... sortFields) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortFields);
        return PageRequest.of(Math.max(0,page-1), PAGE_SIZE, sort);
    }


    public static Sort sortByField(String sortField){
        return Sort.by(Sort.Direction.DESC, sortField, FIELD_ID);
    }

    public static Sort sortDefault(){
        return Sort.by(Sort.Direction.DESC, FIELD_PUBLISHED, FIELD_ID);
    }




}
