package org.be.recommend.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.behavior.repository.UserBehaviorRepository;
import org.be.book.model.Purchase;
import org.be.book.model.Rental;
import org.be.book.repository.PurchaseRepository;
import org.be.book.repository.RentalRepository;
import org.be.recommend.dto.RecommendResponse;
import org.be.recommend.dto.RecommendRequestMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecommendService {
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final RentalRepository rentalRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, RecommendResponse> redisTemplate;
    private static final String REDIS_KEY_PREFIX = "recommend:user:";

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    public RecommendService(UserRepository userRepository,
                            PurchaseRepository purchaseRepository,
                            RentalRepository rentalRepository,
                            UserBehaviorRepository userBehaviorRepository,
                            KafkaProducerService kafkaProducerService,
                            RedisTemplate<String, RecommendResponse> redisTemplate) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.rentalRepository = rentalRepository;
        this.userBehaviorRepository = userBehaviorRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.redisTemplate = redisTemplate;
    }

    public RecommendResponse checkCache(String userId) {
        String redisKey = REDIS_KEY_PREFIX + userId;

        // Redis에서 캐싱된 추천 결과 확인 - 있으면 바로 응담, 없으면 추천 요청
        return redisTemplate.opsForValue().get(redisKey);
    }

    public void recommendBooks(User user) {

        String userId = user.getUserId();

        // 사용자의 구매 도서 조회
        List<RecommendRequestMessage.ReadBook> purchasedBooks = purchaseRepository.findByUser(user).stream()
                .map(Purchase::getBook)
                .map(book -> {
                    RecommendRequestMessage.ReadBook dto = new RecommendRequestMessage.ReadBook();
                    dto.setBookId(book.getId());
                    dto.setTitle(book.getTitle() == null ? "" : book.getTitle());
                    dto.setAuthor(book.getAuthor() == null ? "" : book.getAuthor());
                    dto.setGenre(book.getGenre() == null ? "" : book.getGenre());
                    return dto;
                }).collect(Collectors.toList());

        log.info("구매한 책 수: {}", purchasedBooks.size());
        log.info("구매한 책 목록: {}", purchasedBooks.stream()
                .map(b -> String.format("[title=%s, author=%s, genre=%s]", b.getTitle(), b.getAuthor(), b.getGenre()))
                .collect(Collectors.joining(", "))
        );

        // 사용자의 대여 도서 조회
        List<RecommendRequestMessage.ReadBook> rentedBooks = rentalRepository.findByUser(user).stream()
                .map(Rental::getBook)
                .map(book -> {
                    RecommendRequestMessage.ReadBook dto = new RecommendRequestMessage.ReadBook();
                    dto.setBookId(book.getId());
                    dto.setTitle(book.getTitle() == null ? "" : book.getTitle());
                    dto.setAuthor(book.getAuthor() == null ? "" : book.getAuthor());
                    dto.setGenre(book.getGenre() == null ? "" : book.getGenre());
                    return dto;
                }).collect(Collectors.toList());

        log.info("대여한 책 수: {}", rentedBooks.size());
        log.info("대여한 책 목록: {}", rentedBooks.stream()
                .map(b -> String.format("[title=%s, author=%s, genre=%s]", b.getTitle(), b.getAuthor(), b.getGenre()))
                .collect(Collectors.joining(", "))
        );

        // 전체 도서 목록 리스트 생성
        List<RecommendRequestMessage.ReadBook> readBooks = new ArrayList<>();
        readBooks.addAll(purchasedBooks);
        readBooks.addAll(rentedBooks);

        if (readBooks.isEmpty()) {
            log.warn("사용자 {}가 대여하거나 구매한 책이 없습니다.", userId);
        }

        // 사용자 행동 데이터 조회
        List<RecommendRequestMessage.UserBehavior> behaviors = userBehaviorRepository.findByUser(user).stream()
                .map(behavior -> {
                    RecommendRequestMessage.UserBehavior dto = new RecommendRequestMessage.UserBehavior();
                    dto.setBookId(behavior.getBookId());
                    dto.setStayTime(behavior.getStayTime());
                    dto.setScrollDepth(behavior.getScrollDepth());
                    return dto;
                })
                .collect(Collectors.toList());

        RecommendRequestMessage message = new RecommendRequestMessage();
        message.setUserId(userId);
        message.setReadBooks(readBooks);
        message.setUserBehaviors(behaviors);

        log.info("Spring Boot 추천 요청 전송 - userId={}, 읽은 책 수={}, 행동 데이터 수={}",
                userId, readBooks.size(), behaviors.size());
        log.info("읽은 책 전체 목록 (구매+대여): {}", readBooks.stream()
                .map(b -> String.format("[title=%s, author=%s, genre=%s]", b.getTitle(), b.getAuthor(), b.getGenre()))
                .collect(Collectors.joining(", "))
        );
        log.info("행동 데이터 목록: {}", behaviors.stream()
                .map(b -> String.format("[bookId=%s, stayTime=%s, scrollDepth=%s]", b.getBookId(), b.getStayTime(), b.getScrollDepth()))
                .collect(Collectors.joining(", "))
        );

        // Kafka로 추천 요청 전송
        kafkaProducerService.sendToFlask(message);
    }
}
