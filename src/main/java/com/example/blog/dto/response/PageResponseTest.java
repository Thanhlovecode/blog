package com.example.blog.dto.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseTest<T> {
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private List<T> content;

    public PageResponseTest(Page<T> page) {
        this.content = page.getContent();
        this.pageNo = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

    public static <T> PageResponseTest<T> fromPage(Page<T> page) {
        return new PageResponseTest<>(page);
    }

    public <U> PageResponseTest<U> map(List<U> newContent) {
        return new PageResponseTest<>(
                this.pageNo,
                this.pageSize,
                this.totalPages,
                this.totalElements,
                newContent
        );
    }
}
