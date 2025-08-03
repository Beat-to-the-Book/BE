package org.be.recommend.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommendRequestMessage {
    private String userId;
    private List<ReadBook> readBooks;
    private List<UserBehavior> userBehaviors;
    private Long startTime; // 전송 시작 시점

    @Data
    public static class ReadBook {
        private Long bookId;
        private String title;
        private String author;
        private String genre;
    }

    @Data
    public static class UserBehavior {
        private Long bookId;
        private int stayTime; // seconds
        private int scrollDepth; // percentage
    }
}
