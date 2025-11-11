package org.be.recommend.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.be.auth.service.CustomUserDetails;
import org.be.recommend.dto.RecommendReasonResponse;
import org.be.recommend.dto.RecommendResponse;
import org.be.recommend.service.RecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendController {
    private final RecommendService recommendService;

    private static final Logger log = LoggerFactory.getLogger(RecommendController.class);

    @PostMapping
    @Transactional
    public ResponseEntity<?> recommendBooks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUsername();

        RecommendResponse cachedRecommendations = recommendService.checkCache(userId);

        // Redis에 추천 결과가 있으면 (null이거나 캐시는 존재하나 값이 비어있지 않으면)
        if (cachedRecommendations != null && !cachedRecommendations.getRecommendedBooks().isEmpty()) {
            log.info("[CACHE HIT] 추천 결과 반환 - userId={}", userId);
            return ResponseEntity.ok(cachedRecommendations);
        }

        // Redis에 추천 결과가 없으면 Kafka로 추천 요청을 보낸다
        log.info("추천 결과 없음. Kafka로 추천 요청 전송 - userId={}", userId);
        recommendService.recommendBooks(userDetails.getUser());

        // 추천 결과가 Redis에 저장될 때까지 일정 시간 동안 반복적으로 확인하는 결과 폴링
        // 0.5초 간격으로 30번, 15초 동안 Redis 확인
        int maxAttempts = 30;           // 요청 수
        int delayMillis = 500;         // 한 번 기다리는 시간 (500ms → 0.5초)

        for (int i = 0; i < maxAttempts; i++) {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            RecommendResponse result = recommendService.checkCache(userId);
            if (result != null && result.getRecommendedBooks() != null && !result.getRecommendedBooks().isEmpty()) {
                log.info("[ASYNC POLL SUCCESS] 추천 결과 확보 - userId={}, 시도={}회", userId, i + 1);
                return ResponseEntity.ok(result);  // 성공 시 추천 결과 바로 반환
            }

            log.info("[POLLING] 추천 결과 대기 중 - userId={}, 시도={}", userId, i + 1);
        }

        // 추천 결과가 15초 안에 생성되지 않았거나, 추천 결과 생성 실패
        log.warn("[ASYNC POLL TIMEOUT] 추천 결과 생성 실패 - userId={}", userId);
        return ResponseEntity.status(202) // 202 Accepted
                .body("추천 결과를 생성 중 입니다. 잠시 후 다시 요청하세요.");
    }

    @PostMapping("/reason")
    @Transactional
    public ResponseEntity<?> getRecommendationReasons(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String userId = userDetails.getUsername();

        RecommendResponse cachedRecommendations = recommendService.checkCache(userId);
        if (cachedRecommendations == null || cachedRecommendations.getRecommendedBooks().isEmpty()) {
            return ResponseEntity.status(404).body("추천 결과가 존재하지 않습니다.");
        }

        RecommendReasonResponse reasonResponse = recommendService.fetchRecommendationReasons(userId, cachedRecommendations);
        return ResponseEntity.ok(reasonResponse);
    }
}
