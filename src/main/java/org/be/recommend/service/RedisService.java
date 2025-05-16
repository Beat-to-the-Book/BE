package org.be.recommend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String VIEW_HISTORY_KEY = "view_history:";

    public void saveViewedBook(String userId, Long bookId) {
        redisTemplate.opsForList().leftPush(VIEW_HISTORY_KEY + userId, bookId.toString());
        redisTemplate.expire(VIEW_HISTORY_KEY + userId, Duration.ofDays(7)); // 7일 유지
    }

    public List<String> getViewedBooks(String userId) {
        return redisTemplate.opsForList().range(VIEW_HISTORY_KEY + userId, 0, -1);
    }
}
