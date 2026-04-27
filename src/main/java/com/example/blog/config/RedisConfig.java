package com.example.blog.config;

import com.example.blog. dto.response.PageResponse;
import com.example.blog.dto.response.PostResponseDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io. github.bucket4j.distributed. proxy.ProxyManager;
import io.github.bucket4j. redis.lettuce.cas. LettuceBasedProxyManager;
import io.lettuce. core.RedisClient;
import io.lettuce.core.RedisURI;
import io. lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec. ByteArrayCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.beans.factory.annotation. Value;
import org.springframework. boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer. GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer. Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import static com.example. blog.constants.CacheConstants. CACHE_POST_DETAIL;
import static com.example.blog.constants.CacheConstants.POST_IDS_PAGE_CACHE;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static java.time.Duration. ofHours;
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

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);

        // ✅ SET PASSWORD
        if (password != null && ! password.isEmpty()) {
            config.setPassword(password);
            log.info("✅ Redis password configured for connection factory");
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);

        log.info("✅ Redis connection factory created: {}:{}", host, port);

        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        ObjectMapper redisObjectMapper = createRedisObjectMapper();

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Serializers
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate. setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();

        log.info("✅ RedisTemplate configured");

        return redisTemplate;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer() {
        ObjectMapper cacheObjectMapper = createRedisObjectMapper();

        GenericJackson2JsonRedisSerializer defaultSerializer =
                new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

        Jackson2JsonRedisSerializer<PageResponse> pageResponseSerializer =
                new Jackson2JsonRedisSerializer<>(cacheObjectMapper, PageResponse.class);

        Jackson2JsonRedisSerializer<PostResponseDetail> postDetailSerializer =
                new Jackson2JsonRedisSerializer<>(cacheObjectMapper, PostResponseDetail.class);

        return builder -> builder
                .cacheDefaults(defaultCacheConfig()
                        . serializeValuesWith(fromSerializer(defaultSerializer))
                        .entryTtl(ofHours(2)))
                .withCacheConfiguration(POST_IDS_PAGE_CACHE, defaultCacheConfig()
                        .serializeValuesWith(fromSerializer(pageResponseSerializer))
                        .entryTtl(ofHours(1)))
                .withCacheConfiguration(CACHE_POST_DETAIL, defaultCacheConfig()
                        .serializeValuesWith(fromSerializer(postDetailSerializer))
                        .entryTtl(ofHours(1)));
    }

    @Bean(destroyMethod = "shutdown")
    public RedisClient bucket4jRedisClient() {
        // Build RedisURI với password và authentication
        RedisURI. Builder uriBuilder = RedisURI. builder()
                .withHost(host)
                .withPort(port);

        // ✅ Set password nếu có
        if (password != null && !password.isEmpty()) {
            uriBuilder.withPassword(password.toCharArray());
            log.info("✅ Bucket4j Redis client configured with password");
        }

        RedisURI redisUri = uriBuilder.build();

        RedisClient client = RedisClient.create(redisUri);

        log.info("✅ Bucket4j Redis client created:  {}:{}", host, port);

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

    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper. activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                PROPERTY);
        return redisObjectMapper;
    }
}