package org.be.recommend.controller;

import jakarta.transaction.Transactional;
import org.be.recommend.dto.BookDto;
import org.be.recommend.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {
    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping
    @Transactional
    public ResponseEntity<List<BookDto>> getRecommendations(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername(); // 토큰에서 UserId를 추출

        return ResponseEntity.ok(recommendService.getRecommendations(userId));
    }
}
