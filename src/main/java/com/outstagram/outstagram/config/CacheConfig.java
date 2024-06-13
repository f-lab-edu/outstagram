package com.outstagram.outstagram.config;

import static com.outstagram.outstagram.common.constant.CacheConst.COMMENT;
import static com.outstagram.outstagram.common.constant.CacheConst.IMAGE;
import static com.outstagram.outstagram.common.constant.CacheConst.POST;
import static com.outstagram.outstagram.common.constant.CacheConst.USER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * post의 경우, 리소스의 변경이 거의 일어나지 않으므로 만료 시간을 1시간으로 지정
 * feed의 경우, post의 생성, 수정, 삭제 등으로 리소스의 변경이 자주 일어나므로 만료 시간을 5초로 지정
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.username}")
    private String username;

    @Value("${spring.redis.password}")
    private String password;
    @Bean(name = "redisCacheConnectionFactory")
    RedisConnectionFactory redisCacheConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setUsername(username);
        redisStandaloneConfiguration.setPassword(password);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisCacheManager cacheManager(@Qualifier("redisCacheConnectionFactory") RedisConnectionFactory connectionFactory) {

        // Jackson 라이브러리가 Java8의 LocalDateTime 타입을 직렬화할 수 없는 오류 해결하기 위한 objectMapper 설정 코드
        // LinkedHashMap을 다른 타입으로 casting 할 수 없는 오류 해결하기 위해 DefaultTyping 활성화
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Object.class)
            .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        Map<String, RedisCacheConfiguration> redisCacheConfigMap = new HashMap<>();
        redisCacheConfigMap.put(POST, defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigMap.put(COMMENT, defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigMap.put(IMAGE, defaultConfig.entryTtl(Duration.ofHours(1)));
        redisCacheConfigMap.put(USER, defaultConfig.entryTtl(Duration.ofHours(5)));

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(redisCacheConfigMap)
                .build();
    }
}
