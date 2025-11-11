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
import org.be.recommend.dto.RecommendReasonResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

@Service
public class RecommendService {
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;
    private final RentalRepository rentalRepository;
    private final UserBehaviorRepository userBehaviorRepository;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, RecommendResponse> redisTemplate;

    @Value("${app.redis.use-redis}")
    private boolean useRedis;

    @Value("${app.kafka.use-kafka}")
    private boolean useKafka;

    @Value("${app.flask.recommend-url}")
    private String flaskUrl;

    private static final String REDIS_KEY_PREFIX = "recommend:user:";

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    private final RestTemplate restTemplate = new RestTemplate();

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
        // 캐시 미사용 테스트
        if (!useRedis) {
            log.info("[REDIS OFF] 캐시 미사용 설정 - userId={}", userId);
            return null;
        }

        String redisKey = REDIS_KEY_PREFIX + userId;

        try {
            // Redis에서 캐싱된 추천 결과 확인 - 있으면 바로 응답, 없으면 추천 요청
            RecommendResponse cached = redisTemplate.opsForValue().get(redisKey);

            if (cached != null && cached.getRecommendedBooks() != null && !cached.getRecommendedBooks().isEmpty()) {
                log.info("[CACHE HIT] 추천 결과 반환 - userId={}", userId);
            } else {
                log.info("[CACHE MISS] 추천 결과 없음, 추천 요청 - userId={}", userId);
            }

            return cached;
        } catch (Exception e) {
            log.error("[REDIS ERROR] 역직렬화 실패 또는 Redis 접근 오류 - key={}, 원인={}", redisKey, e.getMessage(), e);
            return null;
        }
    }

    public void recommendBooks(User user) {
        String userId = user.getUserId();

        Map<Long, RecommendRequestMessage.ReadBook> readBookMap = new HashMap<>();

        // 구매 도서 반영
        for (Purchase purchase : purchaseRepository.findByUser(user)) {
            var book = purchase.getBook();
            if (book == null) continue;

            readBookMap.computeIfAbsent(book.getId(), id -> {
                var dto = new RecommendRequestMessage.ReadBook();
                dto.setBookId(book.getId());
                dto.setTitle(book.getTitle() == null ? "" : book.getTitle());
                dto.setAuthor(book.getAuthor() == null ? "" : book.getAuthor());
                dto.setGenre(book.getGenre() == null ? "" : book.getGenre());
                dto.setPrice(book.getPrice() != null ? book.getPrice() : 0);
                dto.setPurchased(true);
                dto.setRented(false);
                return dto;
            }).setPurchased(true);
        }

        // 대여 도서 반영
        for (Rental rental : rentalRepository.findByUser(user)) {
            var book = rental.getBook();
            if (book == null) continue;

            readBookMap.computeIfAbsent(book.getId(), id -> {
                var dto = new RecommendRequestMessage.ReadBook();
                dto.setBookId(book.getId());
                dto.setTitle(book.getTitle() == null ? "" : book.getTitle());
                dto.setAuthor(book.getAuthor() == null ? "" : book.getAuthor());
                dto.setGenre(book.getGenre() == null ? "" : book.getGenre());
                dto.setPrice(book.getPrice() != null ? book.getPrice() : 0);
                dto.setPurchased(false);
                dto.setRented(true);
                return dto;
            }).setRented(true);
        }

        List<RecommendRequestMessage.ReadBook> readBooks = new ArrayList<>(readBookMap.values());

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
                    dto.setTimestamp(behavior.getTimestamp());
                    return dto;
                })
                .collect(Collectors.toList());

        RecommendRequestMessage message = new RecommendRequestMessage();
        message.setUserId(userId);
        message.setReadBooks(readBooks);
        message.setUserBehaviors(behaviors);
        message.setStartTime(System.currentTimeMillis());

        log.info("Spring Boot 추천 요청 전송 - userId={}, 읽은 책 수={}, 행동 데이터 수={}",
                userId, readBooks.size(), behaviors.size());
        log.info("읽은 책 전체 목록 (구매+대여): {}", readBooks.stream()
                .map(b -> String.format("[title=%s, author=%s, genre=%s]", b.getTitle(), b.getAuthor(), b.getGenre()))
                .collect(Collectors.joining(", "))
        );
        log.info("행동 데이터 목록: {}", behaviors.stream()
                .map(b -> String.format("[bookId=%s, stayTime=%s, scrollDepth=%s, timestamp=%s]", b.getBookId(), b.getStayTime(), b.getScrollDepth(), b.getTimestamp()))
                .collect(Collectors.joining(", "))
        );

        // Spring → Flask 요청 전송 직전 로그 (시작 시점 기록)
        long startTime = System.currentTimeMillis();
        log.info("[TO FLASK SEND START] userId={}, startTime={}", userId, startTime);

        if (useKafka) {
            // Kafka로 추천 요청 전송
            log.info("[KAFKA MODE] Kafka 전송 시작 - userId={}", userId);
            kafkaProducerService.sendToFlask(message);
        } else {
            // 직접 Flask 호출
            log.info("[NO-KAFKA MODE] Flask 직접 호출 시작 - userId={}", userId);
            callFlaskDirectly(message);
        }
    }

    public RecommendReasonResponse fetchRecommendationReasons(String userId, RecommendResponse baseResponse) {
        try {
            log.info("[REASON REQUEST] 추천 이유 요청 시작 - userId={}, 추천 결과 수={}", userId, baseResponse.getRecommendedBooks().size());

            // 전달할 JSON 형식으로 직접 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("recommendedBooks", baseResponse.getRecommendedBooks()); // List<Map<String, Object>>여야 함

            log.info("[REASON REQUEST BODY] {}", requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<RecommendReasonResponse> response = restTemplate.exchange(
                    flaskUrl + "/reason", HttpMethod.POST, request, RecommendReasonResponse.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.warn("[FLASK FAIL] 추천 이유 응답 실패 - status={}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("[FLASK ERROR] 추천 이유 요청 중 예외 발생 - {}", e.getMessage());
            return null;
        }
    }

    private void callFlaskDirectly(RecommendRequestMessage message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RecommendRequestMessage> request = new HttpEntity<>(message, headers);

            ResponseEntity<RecommendResponse> response = restTemplate.exchange(
                    flaskUrl, HttpMethod.POST, request, RecommendResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String redisKey = REDIS_KEY_PREFIX + message.getUserId();
                redisTemplate.opsForValue().set(redisKey, response.getBody());
                log.info("[FLASK SUCCESS] 캐싱 완료 - userId={}", message.getUserId());
            } else {
                log.warn("[FLASK FAIL] 추천 응답 실패 - status={}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[FLASK ERROR] 예외 발생 - {}", e.getMessage());
        }
    }
}
