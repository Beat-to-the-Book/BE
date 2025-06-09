package org.be.book.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long id;
    private String title;
    private String content;
    private int rating;
    private Boolean publicVisible; // ✅ 수정
    private String bookTitle;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportResponse from(org.be.book.model.Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .content(report.getContent())
                .rating(report.getRating())
                .publicVisible(report.getPublicVisible()) // ✅ getter 명확히
                .bookTitle(report.getBook().getTitle())
                .authorName(report.getAuthor().getUsername())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}