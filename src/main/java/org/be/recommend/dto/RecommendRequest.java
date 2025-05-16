package org.be.recommend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RecommendRequest {
    private List<BehaviorBook> userBehaviors;

    @Getter
    @Setter
    public static class BehaviorBook {
        private Long bookId;
        private int clickCount;
        private int stayTime;
    }
}
