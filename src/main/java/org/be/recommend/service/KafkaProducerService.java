package org.be.recommend.service;

import org.be.book.model.Book;
import org.be.recommend.dto.BookDto;
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
    public void sendBooksForRecommendation(String userId, List<BookDto> books) {
        List<BookDto> bookDtos = books.stream()
                .map(book -> new BookDto(book.getTitle(), book.getAuthor(), book.getGenre()))
                .toList();

            kafkaTemplate.send(TOPIC, bookData);
        }
    }
}
