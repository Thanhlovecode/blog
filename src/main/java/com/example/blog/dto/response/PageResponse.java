package com.example.blog.dto.response;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import org.springframework.data.domain.Page;

import java.io.Serializable;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse <T> {

    private int pageNo;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private T items;

    public static <T> PageResponse<T> fromPage(Page<?> page,T data) {
        return PageResponse.<T>builder()
                .pageNo(page.getNumber()+1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .items(data)
                .build();
    }

}
