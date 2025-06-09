package org.be.book.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReviewResponse {
    private Long reviewId;
    private String author;
    private int rating;
    private String comment;
    private String bookTitle;
    private LocalDate createdAt;
}