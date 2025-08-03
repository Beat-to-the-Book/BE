package org.be.recommend.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommendResponseMessage {
    private String userId;
    private List<RecommendResponse.RecommendBook> books;
    private Long startTime; // Kafka 전송 시작 시간 (ms 단위)
}
