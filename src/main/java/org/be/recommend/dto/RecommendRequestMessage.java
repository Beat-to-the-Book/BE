package org.be.recommend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecommendRequestMessage {
    private String userId;
    private List<ReadBook> readBooks;
    private List<UserBehavior> userBehaviors;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReadBook {
        private Long bookId;
        private String title;
        private String author;
        private String genre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserBehavior {
        private Long bookId;
        private int stayTime; // seconds
        private int scrollDepth; // percentage
    }
}
