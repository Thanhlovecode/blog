package com.example.blog.config;

import com.example.blog.dto.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static com.example.blog.constants.CacheConstants.POST_IDS_PAGE_CACHE;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofHours;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {



    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    private final ObjectMapper objectMapper;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host,port));
    }

    @Bean
    public RedisTemplate<String,Object> redisTemplate(){
        RedisTemplate<String,Object> redisTemplate= new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        ObjectMapper redisObjectMapper = createRedisObjectMapper();

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        // sử dụng StringRedisSerializer để tuần tự hóa và solve tuần tự hóa các value key redis
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);

        // Key hash cx sử dụng phương thức tuần tự hóa StringRedisSerializer
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Bean
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer() {

        ObjectMapper cacheObjectMapper = createRedisObjectMapper();

        GenericJackson2JsonRedisSerializer defaultSerializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper);
        Jackson2JsonRedisSerializer<PageResponse> pageResponseJackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(cacheObjectMapper, PageResponse.class);

        return builder -> builder.cacheDefaults(defaultCacheConfig()
                        .serializeValuesWith(fromSerializer(defaultSerializer))
                        .entryTtl(ofDays(1)))
                .withCacheConfiguration(POST_IDS_PAGE_CACHE, defaultCacheConfig()
                        .serializeValuesWith(fromSerializer(pageResponseJackson2JsonRedisSerializer))
                        .entryTtl(ofHours(1)));

    }

    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                PROPERTY);
        return redisObjectMapper;
    }

}
