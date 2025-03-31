package org.be.recommend.service;

import org.be.recommend.dto.BookDto;
import org.be.recommend.dto.RecommendationMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, RecommendationMessage> kafkaTemplate;
    private final String TOPIC = "recommendation_topic";

    public KafkaProducerService(KafkaTemplate<String, RecommendationMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final Logger log = LoggerFactory.getLogger(RecommendService.class);

    public void sendBooksForRecommendation(String userId, List<BookDto> books) {
        List<BookDto> bookDtos = books.stream()
                .map(book -> new BookDto(book.getTitle(), book.getAuthor(), book.getGenre()))
                .toList();

        RecommendationMessage message = new RecommendationMessage();
        message.setUserId(userId);
        message.setBooks(bookDtos);

        try {
            log.info("Kafka 전송 데이터: {}", message);
            kafkaTemplate.send(TOPIC, message).get(); // 비동기 전송 후 결과 대기
        } catch (Exception e) {
            throw new RuntimeException("Kafka 메시지 전송 실패: " + e.getMessage());
        }
    }
}
