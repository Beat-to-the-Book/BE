package org.be.recommend.service;

import org.be.recommend.dto.BookDto;
import org.be.recommend.dto.RecommendationResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class KafkaConsumerService {
    private final RedisTemplate<String, List<BookDto>> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "recommend:user:";
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    public KafkaConsumerService(RedisTemplate<String, List<BookDto>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "recommendation_topic", groupId = "recommendation_group")
    public void consumeRecommendation(RecommendationResultMessage message) {
        String userId = message.getUserId();
        List<BookDto> recommendedBooks = message.getBooks();

        log.info("Kafka 추천 결과 수신 - userId={}, 추천 책 수={}", userId, recommendedBooks.size());

        // Redis에 추천 결과 저장 (10분 캐싱)
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + userId, recommendedBooks, Duration.ofMinutes(10));
    }
}
