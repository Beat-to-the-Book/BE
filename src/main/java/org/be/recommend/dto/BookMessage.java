package org.be.recommend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

// KafkaProducerService → Flask
// Kafka를 통해 Flask → Spring 으로 전달되는 메시지 형식
// 유저 ID + 추천된 도서 리스트
// Spring에서는 이 메시지를 받아서 Redis에 캐싱
@Data
@NoArgsConstructor
public class BookMessage {
    private String userId;
    private String title;
    private String author;
    private String genre;
}