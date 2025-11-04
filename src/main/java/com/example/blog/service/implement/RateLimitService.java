package com.example.blog.service.implement;

import com.example.blog.dto.response.RateLimitResponse;
import com.example.blog.enums.RateLimitType;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final ProxyManager<byte[]> proxyManager;

    private static final String KEY_PREFIX = "rate:limit:";


    public RateLimitService(ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public RateLimitResponse tryConsume(String key, RateLimitType rateLimitType) {
        String bucketKey = KEY_PREFIX + key + ":" + rateLimitType.name();

        Bucket bucket = proxyManager.builder()
                .build(bucketKey.getBytes(),
                        rateLimitType::toBucketConfiguration);

        var consumptionResult = bucket.tryConsumeAndReturnRemaining(1);

        if (consumptionResult.isConsumed()) {
            return RateLimitResponse.allowed();
        } else {
            long waitForRefillSeconds = consumptionResult.getNanosToWaitForRefill() / 1_000_000_000;
            return RateLimitResponse.rejected(waitForRefillSeconds);
        }
    }
}
