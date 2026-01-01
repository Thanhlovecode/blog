package com.example.blog.utils;

public class RedisKeys {
    public static final String POST_VIEW_HASH_KEY = "post_views";
    public static final String DIRTY_POST_SET_KEY = "dirty_posts";
    public static final int DEBOUNCE_SECONDS = 600;
    public static final String DEBOUNCE_KEY_PREFIX = "post:view:debounce";
}
