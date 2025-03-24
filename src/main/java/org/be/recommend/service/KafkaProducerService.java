package org.be.recommend.service;

import org.be.book.model.Book;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, Map<String, String>> kafkaTemplate;
    private final String TOPIC = "recommendation_topic";

    public KafkaProducerService(KafkaTemplate<String, Map<String, String>> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBooksForRecommendation(String userId, List<Book> books) {
        for (Book book : books) {
            Map<String, String> bookData = new HashMap<>();
            bookData.put("userId", userId);
            bookData.put("title", book.getTitle());
            bookData.put("author", book.getAuthor());
            bookData.put("genre", book.getGenre());

            kafkaTemplate.send(TOPIC, bookData);
        }
    }
}
