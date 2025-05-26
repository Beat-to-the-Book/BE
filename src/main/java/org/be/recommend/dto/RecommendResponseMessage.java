package org.be.recommend.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommendResponseMessage {
    private String userId;
    private List<RecommendResponse.RecommendBook> books;
}
