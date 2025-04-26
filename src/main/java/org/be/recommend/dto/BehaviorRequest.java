package org.be.recommend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class BehaviorRequest {
    private List<BehaviorBook> books;

    @Getter
    @Setter
    public static class BehaviorBook {
        private String bookId;
        private int clickCount;
        private int stayTime;
    }
}
