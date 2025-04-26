package org.be.recommend.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.be.recommend.dto.RecommendRequestMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final String TOPIC = "flask_recommendation_topic"; // Flask가 구독하는 토픽 이름

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // null 필드 제거
    }

    public void sendToFlask(RecommendRequestMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            log.info("Kafka 전송 데이터(JSON): {}", jsonMessage);

            kafkaTemplate.send(TOPIC, jsonMessage).get(); // 변환된 JSON 문자열을 Kafka로 전송
        } catch (Exception e) {
            throw new RuntimeException("Kafka 메시지 전송 실패: " + e.getMessage(), e);
        }
    }
}
