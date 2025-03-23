package org.be.book.controller;

import org.be.book.dto.CreateBookRequest;
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
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // 관리자가 직접 도서 추가
    @PostMapping("/create")
    public ResponseEntity<Book> createBook(@RequestBody CreateBookRequest createBookRequest) {
        Book savedBook = bookService.saveBook(createBookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }
}
