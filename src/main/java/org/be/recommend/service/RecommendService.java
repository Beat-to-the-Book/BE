package org.be.recommend.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.model.Purchase;
import org.be.book.model.Rental;
import org.be.book.repository.PurchaseRepository;
import org.be.book.repository.RentalRepository;
import org.be.recommend.dto.BookDto;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecommendService {
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final RentalRepository rentalRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, List<BookDto>> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "recommend:user:";

    public RecommendService(UserRepository userRepository,
                            PurchaseRepository purchaseRepository,
                            RentalRepository rentalRepository,
                            KafkaProducerService kafkaProducerService,
                            RedisTemplate<String, List<BookDto>> redisTemplate) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.rentalRepository = rentalRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.redisTemplate = redisTemplate;
    }

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    public List<BookDto> getRecommendations(String userId) {
        String redisKey = REDIS_KEY_PREFIX + userId;

        // Redis에서 캐싱된 추천 결과 확인 - 있으면 바로 응담, 없으면 추천 요청
        List<BookDto> cachedRecommendations = redisTemplate.opsForValue().get(redisKey);
            return cachedRecommendations;
        }

        log.info("[CACHE MISS] Redis에 추천 없음. Kafka로 추천 요청 예정 - userId={}", userId);

        // JWT 토큰에서 사용자 추출
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        // 사용자가 대여 또는 구매한 책 조회
        List<Book> books = purchaseRepository.findByUser(user).stream()
        // 사용자의 구매 도서 조회
        List<BookDto> purchasedBooks = purchaseRepository.findByUser(user).stream()
                .map(Purchase::getBook)
                .map(BookDto::from)
                .collect(Collectors.toList());

        log.info("구매한 책 수: {}, 목록: {}", purchasedBooks.size(),
                purchasedBooks.stream().map(BookDto::getTitle).toList());

        // 사용자의 대여 도서 조회
        List<BookDto> rentedBooks = rentalRepository.findByUser(user).stream()
                .map(Rental::getBook)
                .map(BookDto::from)
                .collect(Collectors.toList());

        log.info("대여한 책 수: {}, 목록: {}", rentedBooks.size(),
                rentedBooks.stream().map(BookDto::getTitle).toList());

        // 전체 도서 목록
        List<BookDto> books = purchasedBooks;
        books.addAll(rentedBooks);

        if (books.isEmpty()) {
            log.warn("추천할 책이 없음 - userId={}", userId);
            throw new RuntimeException(userId + " 사용자가 대여하거나 구매한 책이 없습니다.");
        }

        log.info("Kafka로 추천 요청 전송 - userId={}, 총 책 수={}", userId, books.size());

        // Kafka로 추천 요청 전송
        kafkaProducerService.sendBooksForRecommendation(userId, books);

        return List.of(); // Kafka 응답은 비동기 처리
    }
}
