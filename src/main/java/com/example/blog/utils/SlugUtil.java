package com.example.blog.utils;

import com.github.slugify.Slugify;

public class SlugUtil {
    private static final Slugify slugify = Slugify.builder().build();

    public static String toSlug(String input) {
        return slugify.slugify(input);
    }
}
