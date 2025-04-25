package org.be.book.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.be.book.dto.AddBookRequest;
import org.be.book.model.Book;
import org.be.book.service.BookCrawlingService;
import org.be.book.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookCrawlingService bookCrawlingService;

    // 모든 도서 조회
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // 한 권 호출
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // 특정 도서 조회 (검색)
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

    // 테스트용 수동 크롤링 호출 API
    @PostMapping("/crawl")
    public ResponseEntity<String> crawlBooks() {
        try {
            bookCrawlingService.crawlBooks();
            return ResponseEntity.ok("크롤링 성공");
        } catch (Exception e) {
            log.error("크롤링 중 에러 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("크롤링 실패");
        }
    }
}
