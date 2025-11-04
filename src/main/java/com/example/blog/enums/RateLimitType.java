package com.example.blog.enums;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;

import java.time.Duration;

public enum RateLimitType {

    LOGIN(
            Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofMinutes(15))
                    .build()
    ),

    REGISTER(
            Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(15))
                    .build()
    ),

    FORGOT_PASSWORD(
            Bandwidth.builder()
                    .capacity(3)
                    .refillGreedy(3, Duration.ofHours(1))
                    .build()
    ),

    RESET_PASSWORD(
            Bandwidth.builder()
                    .capacity(3)
                    .refillGreedy(3, Duration.ofHours(1))
                    .build()
    ),

    SEARCH_POST(
            Bandwidth.builder()
                    .capacity(10)
                    .refillGreedy(10, Duration.ofMinutes(5))
                    .build()
    ),

    VERIFY_EMAIL(
            Bandwidth.builder()
                    .capacity(5)
                    .refillGreedy(5, Duration.ofHours(1))
                    .build()
    ),

    VERIFY_OTP(
            Bandwidth.builder()
                    .capacity(5)
                    .refillGreedy(5, Duration.ofHours(1))
                    .build()
    ),

    // 2. Content Creation
    CREATE_POST(
            Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofHours(1))
                    .build()
    ),


    CREATE_COMMENT(
            Bandwidth.builder()
                    .capacity(30)
                    .refillGreedy(30, Duration.ofHours(1))
                    .build()
    ),



    // Reactions
    LIKE_POST(
            Bandwidth.builder()
                    .capacity(200)
                    .refillGreedy(200, Duration.ofHours(1))
                    .build()
    ),

    CLAP_POST(
            Bandwidth.builder()
                    .capacity(200)
                    .refillGreedy(200, Duration.ofHours(1))
                    .build()
    );

    private final Bandwidth bandwidth;

    RateLimitType(Bandwidth bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Bandwidth getBandwidth() {
        return bandwidth;
    }

    public BucketConfiguration toBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(bandwidth)
                .build();
    }
}