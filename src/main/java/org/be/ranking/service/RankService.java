// org/be/ranking/service/RankService.java
package org.be.ranking.service;

import lombok.RequiredArgsConstructor;
import org.be.ranking.dto.BooksRankResponse;
import org.be.ranking.dto.PointsRankResponse;
import org.be.ranking.repository.RankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankRepository rankRepository;

    private LocalDateTime startOfMonth(int year, int month) {
        return LocalDate.of(year, month, 1).atStartOfDay();
    }

    private LocalDateTime endOfMonth(int year, int month) {
        return startOfMonth(year, month).plusMonths(1);
    }

    /** ✅ 월간 포인트 랭킹 (dense rank + TOP10) */
    @Transactional(readOnly = true)
    public List<PointsRankResponse> getMonthlyPointsRanking(int year, int month) {
        var start = startOfMonth(year, month);
        var end = endOfMonth(year, month);

        var rows = rankRepository.findMonthlyPointsAgg(start, end);

        rows.sort(Comparator.comparing(RankRepository.PointsAgg::getPoints).reversed()
                .thenComparing(RankRepository.PointsAgg::getUsername));

        List<PointsRankResponse> result = new ArrayList<>();
        long prevPoints = Long.MIN_VALUE;
        int rank = 0;

        for (var r : rows) {
            long pts = r.getPoints() == null ? 0L : r.getPoints();
            if (pts != prevPoints) {
                rank += 1;
                prevPoints = pts;
            }
            if (rank > 10) break;
            result.add(new PointsRankResponse(rank, r.getUserId(), r.getUsername(), pts));
        }
        return result;
    }

    /** ✅ 월간 구매/대여 랭킹 (dense rank + TOP10) */
    @Transactional(readOnly = true)
    public List<BooksRankResponse> getMonthlyBooksRanking(int year, int month) {
        var start = startOfMonth(year, month);
        var end = endOfMonth(year, month);

        var rows = rankRepository.findMonthlyBooksAgg(start, end);

        rows.sort(Comparator.comparing(RankRepository.BooksAgg::getTotalCount).reversed()
                .thenComparing(RankRepository.BooksAgg::getUsername));

        List<BooksRankResponse> result = new ArrayList<>();
        long prevCount = Long.MIN_VALUE;
        int rank = 0;

        for (var r : rows) {
            long total = r.getTotalCount() == null ? 0L : r.getTotalCount();
            long purchase = r.getPurchaseCount() == null ? 0L : r.getPurchaseCount();
            long rental = r.getRentalCount() == null ? 0L : r.getRentalCount();

            if (total != prevCount) {
                rank += 1;
                prevCount = total;
            }
            if (rank > 10) break;

            result.add(new BooksRankResponse(rank, r.getUserId(), r.getUsername(), total, purchase, rental));
        }
        return result;
    }
}