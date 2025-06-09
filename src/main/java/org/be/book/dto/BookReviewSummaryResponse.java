package org.be.book.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookReviewSummaryResponse {
    private Long bookId;
    private double averageRating;
    private List<ReviewResponse> reviews;
}