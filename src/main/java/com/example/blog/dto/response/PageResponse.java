package com.example.blog.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PageResponse <T>  implements Serializable{

    private int pageNo;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private List<T> content;


    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageNo = page.getNumber()+1;
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

    public static <T> PageResponse<T> buildPage(Page<T> page) {
        return new PageResponse<>(page);
    }

    public static PageResponse<PostResponse> empty() {
        return PageResponse.<PostResponse>builder()
                .content(List.of())
                .pageNo(1)
                .pageSize(0)
                .totalElements(0)
                .totalPages(0)
                .build();
    }

    public <U> PageResponse<U> map(List<U> newContent) {
        return PageResponse.<U>builder()
                .content(newContent)
                .pageNo(this.pageNo)
                .pageSize(this.pageSize)
                .totalElements(this.totalElements)
                .totalPages(this.totalPages)
                .build();
    }

    public static <T> PageResponse<T> fromPage(Page<?> page,List<T> data) {
        return PageResponse.<T>builder()
                .pageNo(page.getNumber()+1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(data)
                .build();
    }


}
