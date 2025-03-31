package org.be.recommend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Flask → KafkaConsumerService
// Spring → Flask 로 책 추천을 요청할 때 사용하는 메시지 타입
// List<BookMessage> 형식으로 Kafka에 보낸다면, Flask에서 userId + 유저가 읽은 책 리스트를 수신
@Data
@NoArgsConstructor
public class RecommendationMessage {
    private String userId;
    private List<BookDto> books;
}
