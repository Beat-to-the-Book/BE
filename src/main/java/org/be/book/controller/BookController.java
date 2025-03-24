package org.be.book.controller;

import org.be.book.dto.AddBookRequest;
import org.be.book.model.Book;
import org.be.book.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book")
public class BookController {
    @Autowired
    private BookService bookService;

    // 모든 도서 조회
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // 특정 도서 조회
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String keyword) {
        // 책 제목에 키워드가 포함된 도서 조회
        return ResponseEntity.ok(bookService.searchBooksByTitle(keyword));
    }

    // 관리자가 직접 도서 추가
    @PostMapping("/add")
    public ResponseEntity<Book> createBook(@RequestBody AddBookRequest addBookRequest) {
        Book savedBook = bookService.saveBook(addBookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }
}
