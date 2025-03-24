package org.be.recommend.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.model.Book;
import org.be.book.model.Purchase;
import org.be.book.model.Rental;
import org.be.book.repository.PurchaseRepository;
import org.be.book.repository.RentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendService {
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final RentalRepository rentalRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, List<Book>> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "recommend:user:";

    public RecommendService(UserRepository userRepository,
                            PurchaseRepository purchaseRepository,
                            RentalRepository rentalRepository,
                            KafkaProducerService kafkaProducerService,
                            RedisTemplate<String, List<Book>> redisTemplate) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.rentalRepository = rentalRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.redisTemplate = redisTemplate;
    }

    public List<Book> getRecommendations(String userId) {
        String redisKey = REDIS_KEY_PREFIX + userId;

        // Redis에서 캐싱된 추천 결과 확인
        List<Book> cachedRecommendations = redisTemplate.opsForValue().get(redisKey);
        if (cachedRecommendations != null) {
            return cachedRecommendations;
        }

        // JWT 토큰에서 추출한 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        // 사용자가 대여 또는 구매한 책 조회
        List<Book> books = purchaseRepository.findByUser(user).stream()
                .map(Purchase::getBook)
                .collect(Collectors.toList());

        books.addAll(rentalRepository.findByUser(user).stream()
                .map(Rental::getBook)
                .collect(Collectors.toList()));

        if (books.isEmpty()) {
            throw new RuntimeException(userId + "사용자가 대여하거나 구매한 책이 없습니다.");
        }

        // Kafka로 추천 요청 전송
        kafkaProducerService.sendBooksForRecommendation(userId, books);

        return List.of(); // Kafka 결과를 기다려야 하므로 빈 리스트 반환
    }
}
