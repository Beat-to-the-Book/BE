package org.be.recommend.controller;

import jakarta.transaction.Transactional;
import org.be.recommend.dto.BehaviorRequest;
import org.be.recommend.dto.BookDto;
import org.be.recommend.service.RecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> recommendBooks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody BehaviorRequest behaviorRequest) {

        String userId = userDetails.getUsername();
        List<BookDto> cachedRecommendations = recommendService.checkCache(userId);

        // null이거나 캐시는 존재하나 값이 비어있지 않으면
        if (cachedRecommendations != null && !cachedRecommendations.isEmpty()) {
            // Redis에 추천 결과가 있으면 바로 반환
            log.info("[CACHE HIT] 추천 결과 반환 - userId={}", userId);
            return ResponseEntity.ok(cachedRecommendations);
        }

        // Redis에 없으면 Kafka로 추천 요청을 보낸다
        log.info("추천 결과가 비어 있음. 추천 요청 중 - userId={}", userId);
        recommendService.recommendBooks(userId, behaviorRequest);

        // 추천 생성 중이니까 "추천 생성 요청 완료" 메시지를 응답
        return ResponseEntity.ok("추천 생성 요청 완료. 잠시 후 다시 요청하세요.");
    }

}
