package org.be.recommend.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.be.recommend.dto.RecommendReasonResponse;
import org.be.recommend.dto.RecommendResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public RedisTemplate<String, RecommendResponse> redisTemplateRecommendResponse(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, RecommendResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<RecommendResponse> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper(), RecommendResponse.class);

        template.setValueSerializer(valueSerializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, RecommendReasonResponse> redisTemplateRecommendReason(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, RecommendReasonResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<RecommendReasonResponse> valueSerializer =
                new Jackson2JsonRedisSerializer<>(RecommendReasonResponse.class);

        template.setValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
