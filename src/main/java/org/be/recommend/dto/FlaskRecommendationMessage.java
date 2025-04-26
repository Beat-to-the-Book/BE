package org.be.recommend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FlaskRecommendationMessage {
    private String userId;
    private List<ReadBook> readBooks;
    private List<BehaviorBook> behavior;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReadBook {
        private String title;
        private String author;
        private String genre;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class BehaviorBook {
        private String bookId;
        private int clickCount;
        private int stayTime;
    }
}
