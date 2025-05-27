package org.be.recommend.dto;

import lombok.*;

import java.util.List;

// 도서 정보 전달을 위한 dto
@Data
public class RecommendResponse {
    private List<RecommendBook> recommendedBooks;

    @Data
    public static class RecommendBook {
        private Long bookId;
        private String title;
        private String author;
        private String coverImageUrl;

        public RecommendBook() {}

        public RecommendBook(Long bookId, String title, String author, String coverImageUrl) {
            this.bookId = bookId;
            this.title = title;
            this.author = author;
            this.coverImageUrl = coverImageUrl;
        }
    }
}

