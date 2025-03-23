package org.be.action.service;

import org.be.action.dto.ActionDto;
import org.be.action.model.Action;
import org.be.action.repository.ActionRepository;
import org.be.book.repository.BookRepository;
import org.be.auth.repository.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final KafkaTemplate<String, ActionDto> kafkaTemplate;

    public ActionService(ActionRepository actionRepository, UserRepository userRepository, BookRepository bookRepository,
                         KafkaTemplate<String, ActionDto> kafkaTemplate) {
        this.actionRepository = actionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // 특정 사용자의 모든 행동 조회
    public List<Action> getUserActions(Long userId) {
        return actionRepository.findByUserId(userId);
    }

    // Kafka에 데이터 전송
    public void sendUserActionToKafka(ActionDto userActionDto) {
        kafkaTemplate.send("user-action-topic", userActionDto);
    }
}
