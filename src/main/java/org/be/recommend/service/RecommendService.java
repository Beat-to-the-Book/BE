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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.*;
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

    private final RedisTemplate<String, RecommendResponse> recommendRedisTemplate;
    private final RedisTemplate<String, RecommendReasonResponse> reasonRedisTemplate;

    @Value("${app.redis.use-redis}")
    private boolean useRedis;

    @Value("${app.kafka.use-kafka}")
    private boolean useKafka;

    @Value("${app.flask.recommend-url}")
    private String flaskUrl;

    private static final String REDIS_KEY_PREFIX = "recommend:user:";

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public RecommendService(
            UserRepository userRepository,
            PurchaseRepository purchaseRepository,
            RentalRepository rentalRepository,
            UserBehaviorRepository userBehaviorRepository,
            KafkaProducerService kafkaProducerService,
            @Qualifier("redisTemplateRecommendResponse") RedisTemplate<String, RecommendResponse> recommendRedisTemplate,
            @Qualifier("redisTemplateRecommendReason") RedisTemplate<String, RecommendReasonResponse> reasonRedisTemplate
    ) {
        this.userRepository = userRepository;
        this.purchaseRepository = purchaseRepository;
        this.rentalRepository = rentalRepository;
        this.userBehaviorRepository = userBehaviorRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.recommendRedisTemplate = recommendRedisTemplate;
        this.reasonRedisTemplate = reasonRedisTemplate;
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
            RecommendResponse cached = recommendRedisTemplate.opsForValue().get(redisKey);

            if (cached != null && cached.getRecommendedBooks() != null && !cached.getRecommendedBooks().isEmpty()) {
                log.info("[CACHE HIT] 추천 결과 반환 - userId={}", userId);
            } else {
                log.info("[CACHE MISS] 추천 결과 없음, 추천 요청 중 - userId={}", userId);
            }

            return cached;
        } catch (Exception e) {
            log.error("[REDIS ERROR] 추천 결과 조회 실패 - key={}, 원인={}", redisKey, e.getMessage(), e);
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
                dto.setTitle(Optional.ofNullable(book.getTitle()).orElse(""));
                dto.setAuthor(Optional.ofNullable(book.getAuthor()).orElse(""));
                dto.setGenre(Optional.ofNullable(book.getGenre()).orElse(""));
                dto.setPrice(Optional.ofNullable(book.getPrice()).orElse(0.0));
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
                dto.setTitle(Optional.ofNullable(book.getTitle()).orElse(""));
                dto.setAuthor(Optional.ofNullable(book.getAuthor()).orElse(""));
                dto.setGenre(Optional.ofNullable(book.getGenre()).orElse(""));
                dto.setPrice(Optional.ofNullable(book.getPrice()).orElse(0.0));
                dto.setPurchased(false);
                dto.setRented(true);
                return dto;
            }).setRented(true);
        }

        List<RecommendRequestMessage.ReadBook> readBooks = new ArrayList<>(readBookMap.values());

        // 사용자 행동 데이터
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

        log.info("[RECOMMEND REQUEST] userId={}, 읽은 책={}, 행동데이터={}", userId, readBooks.size(), behaviors.size());

        if (useKafka) {
            // Kafka로 추천 요청 전송
            log.info("[KAFKA MODE] Kafka로 추천 요청 전송");
            kafkaProducerService.sendToFlask(message);
        } else {
            // 직접 Flask 호출
            log.info("[NO-KAFKA MODE] Flask로 직접 전송");
            callFlaskDirectly(message);
        }
    }

    public RecommendReasonResponse recommendationReasons(String userId, RecommendResponse baseResponse) {
        String redisKey = "reason:model:user:" + userId;

        try {
            log.info("[RECOMMEND REASON REQUEST] 추천 이유 요청 - userId={}, 추천 결과 수={}", userId, baseResponse.getRecommendedBooks().size());

            RecommendReasonResponse reasonResponse = reasonRedisTemplate.opsForValue().get(redisKey);

            if (reasonResponse != null) {
                log.info("[CACHE HIT] 추천 이유 조회 성공 - userId={}", userId);
                return reasonResponse;
            } else {
                log.warn("[CACHE MISS] 추천 이유 캐시 없음 - userId={}", userId);
                return null;
            }
        } catch (Exception e) {
            log.error("[REDIS ERROR] 추천 이유 조회 실패 - key={}, 원인={}", redisKey, e.getMessage());
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
                recommendRedisTemplate.opsForValue().set(redisKey, response.getBody());
                log.info("[FLASK SUCCESS] 추천 결과 캐싱 완료 - userId={}", message.getUserId());
            } else {
                log.warn("[FLASK FAIL] 추천 응답 실패 - status={}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[FLASK ERROR] 예외 발생 - {}", e.getMessage());
        }
    }
}
