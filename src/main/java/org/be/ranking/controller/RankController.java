// org/be/ranking/controller/RankController.java
package org.be.ranking.controller;

import lombok.RequiredArgsConstructor;
import org.be.ranking.dto.BooksRankResponse;
import org.be.ranking.dto.PointsRankResponse;
import org.be.ranking.service.RankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @GetMapping("/points")
    public ResponseEntity<List<PointsRankResponse>> getPointsRank(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(rankService.getMonthlyPointsRanking(year, month));
    }

    @GetMapping("/books")
    public ResponseEntity<List<BooksRankResponse>> getBooksRank(
            @RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(rankService.getMonthlyBooksRanking(year, month));
    }
}