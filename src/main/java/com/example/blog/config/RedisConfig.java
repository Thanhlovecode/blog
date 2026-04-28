package com.example.blog.config;

import com.example.blog.dto.response.PageResponse;
import com.example.blog.dto.response.PostResponseDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static com.example.blog.constants.CacheConstants.CACHE_POST_DETAIL;
import static com.example.blog.constants.CacheConstants.POST_IDS_PAGE_CACHE;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    private final ObjectMapper objectMapper;



    /**
     * RedisCacheManager for @Cacheable — uses typed Jackson2JsonRedisSerializer
     * (NO GenericJackson2JsonRedisSerializer, NO reflection, NO @class field)
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer() {
        // Typed serializers: concrete class → no @class metadata, no reflection
        Jackson2JsonRedisSerializer<PageResponse> pageResponseSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, PageResponse.class);

        Jackson2JsonRedisSerializer<PostResponseDetail> postDetailSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, PostResponseDetail.class);

        return builder -> builder
                .cacheDefaults(defaultCacheConfig()
                        .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(fromSerializer(pageResponseSerializer))
                        .entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration(POST_IDS_PAGE_CACHE, defaultCacheConfig()
                        .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(fromSerializer(pageResponseSerializer))
                        .entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(CACHE_POST_DETAIL, defaultCacheConfig()
                        .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(fromSerializer(postDetailSerializer))
                        .entryTtl(Duration.ofHours(1)));
    }

    // ──────────────────────────────────────────────
    // Bucket4j Rate Limiting — Lettuce Client
    // ──────────────────────────────────────────────

    @Bean(destroyMethod = "shutdown")
    public RedisClient bucket4jRedisClient() {
        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withHost(host)
                .withPort(port);

        if (password != null && !password.isEmpty()) {
            uriBuilder.withPassword(password.toCharArray());
            log.info("✅ Bucket4j Redis client configured with password");
        }

        RedisURI redisUri = uriBuilder.build();
        RedisClient client = RedisClient.create(redisUri);
        log.info("✅ Bucket4j Redis client created: {}:{}", host, port);

        return client;
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<byte[], byte[]> bucket4jRedisConnection(
            RedisClient bucket4jRedisClient) {

        StatefulRedisConnection<byte[], byte[]> connection =
                bucket4jRedisClient.connect(ByteArrayCodec.INSTANCE);

        try {
            String pong = connection.sync().ping();
            log.info("✅ Bucket4j Redis connection established: {}", pong);
        } catch (Exception e) {
            log.error("❌ Failed to verify Bucket4j Redis connection", e);
            throw new RuntimeException("Cannot connect to Redis for Bucket4j", e);
        }

        return connection;
    }

    @Bean
    public ProxyManager<byte[]> bucket4jProxyManager(
            StatefulRedisConnection<byte[], byte[]> bucket4jRedisConnection) {

        ProxyManager<byte[]> proxyManager = LettuceBasedProxyManager
                .builderFor(bucket4jRedisConnection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                Duration.ofHours(1)
                        )
                )
                .build();

        log.info("✅ Bucket4j ProxyManager configured");
        return proxyManager;
    }
}