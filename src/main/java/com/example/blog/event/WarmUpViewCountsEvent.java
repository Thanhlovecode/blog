package com.example.blog.event;

import java.util.Map;

public record WarmUpViewCountsEvent(Map<Long,Long> postIdViewCounts) {
}
