package org.be.book.service;

import org.be.book.dto.AddBookRequest;
import org.be.book.model.Book;
import org.be.book.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 모든 도서 조회
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // 책 제목에 키워드가 포함된 도서 조회
    public List<Book> searchBooksByTitle(String keyword) {
        return bookRepository.findAllByTitleContaining(keyword);
    }

    // 관리자가 직접 도서 추가
    public Book saveBook(AddBookRequest request) {
        boolean exists = bookRepository.existsByTitleAndAuthor(request.getTitle(), request.getAuthor());
        if (exists) {
            return null;
        }

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setPrice(request.getPrice());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishedDate());
        book.setLeftCoverImageUrl(request.getLeftCoverImageUrl());
        book.setFrontCoverImageUrl(request.getFrontCoverImageUrl());

        return bookRepository.save(book);
    }
}
