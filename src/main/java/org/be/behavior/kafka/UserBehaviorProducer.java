package org.be.behavior.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.be.behavior.dto.UserBehaviorRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBehaviorProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(UserBehaviorProducer.class);
    private static final String TOPIC = "user_behavior_log";

    public void sendLog(UserBehaviorRequestMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(TOPIC, json);
            log.info("Kafka 메시지 전송 성공: {}", json);
        } catch (Exception e) {
            log.error("Kafka 전송 실패", e);
        }
    }
}
