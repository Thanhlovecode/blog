package com.example.blog.utils;

public class RedisKeys {
    public static final String VIEW_COUNT_PREFIX = "view_count:";
    public static final String POST_META_PREFIX = "post:meta:";
    public static final String DIRTY_POST_SET_KEY = "dirty_posts";
    public static final int VIEW_COUNT_TTL = 86400; // 1 day
    public static final int DEBOUNCE_SECONDS = 600;
    public static final String DEBOUNCE_KEY_PREFIX = "post:view:debounce";
}
