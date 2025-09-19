package com.example.blog.controller;

import com.example.blog.dto.request.TagUpdateRequest;
import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponse;
import com.example.blog.dto.response.ResponseData;
import com.example.blog.dto.response.TagResponse;
import com.example.blog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseData<TagResponse> createTag(@RequestParam String name,
                                          @RequestParam("file") MultipartFile file) {
        TagResponse tagResponse = tagService.addTag(name,file);
        return ResponseData.<TagResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tag created successfully")
                .data(tagResponse)
                .build();
    }


    @GetMapping
    public ResponseData<PageResponse<List<TagResponse>>> getTags(
            @RequestParam int page) {
        return ResponseData.successWithData("Get all tags successfully",
                tagService.getAllTags(page));
    }



    @PutMapping("/{slug}")
    public ResponseData<Void> updateTag(@PathVariable String slug,
            @RequestBody @Valid TagUpdateRequest request) {
        tagService.updateTag(slug, request);
        return ResponseData.successWithMessage(
                "Tag Updated Successfully", HttpStatus.OK
        );
    }
}
