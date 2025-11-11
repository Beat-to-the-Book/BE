package org.be.recommend.service;

import org.be.recommend.dto.RecommendResponse;
import org.be.recommend.dto.RecommendResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class KafkaConsumerService {
    private final RedisTemplate<String, RecommendResponse> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "recommend:user:";
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Value("${app.redis.use-redis}")
    private boolean useRedis;

    public KafkaConsumerService(RedisTemplate<String, RecommendResponse> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "recommendation_topic", groupId = "recommendation_group")
    public void consumeRecommendation(RecommendResponseMessage message) {
        String userId = message.getUserId();

        RecommendResponse recommendResponse = new RecommendResponse();
        recommendResponse.setRecommendedBooks(message.getBooks());

        if (useRedis) {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + userId, recommendResponse, Duration.ofMinutes(10));
            log.info("[REDIS WRITE] 추천 결과 저장 - userId={}", userId);
        } else {
            log.info("[REDIS OFF] Redis 저장 생략 - userId={}", userId);
        }

        long receivedTime = System.currentTimeMillis();
        Long startTime = message.getStartTime();
        if (startTime != null) {
            long duration = receivedTime - startTime;
            log.info("[KAFKA CONSUME COMPLETE] userId={}, 총 소요 시간={}ms, 추천 개수={}",
                    userId, duration, recommendResponse.getRecommendedBooks().size());
        } else {
            log.info("[KAFKA CONSUME COMPLETE] userId={}, 추천 개수={}",
                    userId, recommendResponse.getRecommendedBooks().size());
        }
    }
}
