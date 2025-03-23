package org.be.recommend.service;

import org.be.book.model.Book;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KafkaConsumerService {
    private final RedisTemplate<String, List<Book>> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "recommend:user:";

    public KafkaConsumerService(RedisTemplate<String, List<Book>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "recommendation_topic", groupId = "recommendation_group")
    public void consumeRecommendation(Map<String, Object> recommendationData) {
        Long userId = Long.valueOf(recommendationData.get("userId").toString());
        List<Book> recommendedBooks = (List<Book>) recommendationData.get("books");

        // Redis에 추천 결과 저장 (10분 캐싱)
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + userId, recommendedBooks);
    }
}
