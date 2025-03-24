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
    public Book saveBook(AddBookRequest addBookRequest) {
        Book book = new Book();
        book.setTitle(addBookRequest.getTitle());
        book.setAuthor(addBookRequest.getAuthor());
        book.setGenre(addBookRequest.getGenre());
        book.setPrice(addBookRequest.getPrice());

        return bookRepository.save(book);
    }
}
