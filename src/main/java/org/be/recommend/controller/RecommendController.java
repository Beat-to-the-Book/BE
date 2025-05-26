package org.be.recommend.controller;

import jakarta.transaction.Transactional;
import org.be.recommend.dto.RecommendResponse;
import org.be.recommend.service.RecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {
    private final RecommendService recommendService;

    private static final Logger log = LoggerFactory.getLogger(RecommendController.class);

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    // 클라이언트가 POST로 추천 요청 보내는 엔드포인트
    @PostMapping
    @Transactional
    public ResponseEntity<?> recommendBooks(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        RecommendResponse cachedRecommendations = recommendService.checkCache(userId);

        // null이거나 캐시는 존재하나 값이 비어있지 않으면
        if (cachedRecommendations != null && cachedRecommendations.getRecommendedBooks() != null && !cachedRecommendations.getRecommendedBooks().isEmpty()) {
            // Redis에 추천 결과가 있으면 바로 반환
            log.info("[CACHE HIT] 추천 결과 반환 - userId={}", userId);

            return ResponseEntity.ok(cachedRecommendations);
        }

        // Redis에 추천 결과가 없으면 Kafka로 추천 요청을 보낸다
        recommendService.recommendBooks(userId);

        // 추천 생성 중이니까 "추천 생성 요청 완료" 메시지를 응답
        // 추천 결과가 아직 생성되지 않았음을 알려줌
        return ResponseEntity.status(202) // 202 Accepted
                .body("추천 결과를 생성 중입니다. 잠시 후 다시 요청하세요.");
    }

}
