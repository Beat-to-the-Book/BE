package org.be.recommend.service;

import org.be.book.model.Book;
import org.be.recommend.dto.BookDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumerService {
    private final RedisTemplate<String, List<BookDto>> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "recommend:user:";

    public KafkaConsumerService(RedisTemplate<String, List<BookDto>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "recommendation_topic", groupId = "recommendation_group")
    public void consumeRecommendation(Map<String, Object> recommendationData) {
        Long userId = Long.valueOf(recommendationData.get("userId").toString());
        List<Book> recommendedBooks = (List<Book>) recommendationData.get("books");
        String userId = message.getUserId();
        List<BookDto> recommendedBooks = message.getBooks();

        // Redis에 추천 결과 저장 (10분 캐싱)
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + userId, recommendedBooks);
    }
}
