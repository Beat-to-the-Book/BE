package org.be.book.service;

import org.be.book.dto.CreateBookRequest;
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

    // 특정 도서 조회
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Book not found with id: " + id));
    }

    // 관리자가 직접 도서 추가
    public Book saveBook(CreateBookRequest createBookRequest) {
        Book book = new Book();
        book.setTitle(createBookRequest.getTitle());
        book.setAuthor(createBookRequest.getAuthor());
        book.setGenre(createBookRequest.getGenre());
        book.setPrice(createBookRequest.getPrice());

        return bookRepository.save(book);
    }
}
