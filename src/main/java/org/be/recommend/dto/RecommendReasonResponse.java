package org.be.recommend.dto;

import lombok.Data;
import java.util.List;

// 추천 도서 이유 전달을 위한 dto
@Data
public class RecommendReasonResponse {
    private List<ReasonBook> booksWithReason;

    @Data
    public static class ReasonBook {
        private Long bookId;
        private String title;
        private String author;
        private String coverImageUrl;
        private String reason;
    }
}
