package com.capd.capdbackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {

        // Redis 템플릿 객체 생성
        RedisTemplate<String, String> template = new RedisTemplate<>();

        // localhost:6379 연결
        template.setConnectionFactory(connectionFactory);

        // Redis에 저장할때 키,값을 문자열로 저장
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}
