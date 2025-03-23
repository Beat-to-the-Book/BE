package org.be.recommend.controller;

import org.be.book.model.Book;
import org.be.recommend.service.RecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {
    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Book>> getRecommendations(@PathVariable Long userId) {
        return ResponseEntity.ok(recommendService.getRecommendations(userId));
    }
}
