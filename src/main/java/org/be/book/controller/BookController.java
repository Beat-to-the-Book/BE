package org.be.book.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.be.book.dto.AddBookRequest;
import org.be.book.model.Book;
import org.be.book.service.BookCrawlingService;
import org.be.book.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

@Tag(name = "도서 API", description = "도서 검색, 등록, 크롤링 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookCrawlingService bookCrawlingService;

    // 모든 도서 조회
    @Operation(summary = "모든 도서 목록 조회", description = "등록된 모든 도서를 조회합니다.")
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // 특정 도서 조회
    @Operation(summary = "도서 검색", description = "도서 제목에 포함된 키워드로 도서를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @Parameter(description = "도서 제목 키워드", example = "자바")
            @RequestParam String keyword)
    {
        // 책 제목에 키워드가 포함된 도서 조회
        return ResponseEntity.ok(bookService.searchBooksByTitle(keyword));
    }

    // 관리자가 직접 도서 추가
    @Operation(summary = "도서 등록", description = "관리자가 도서를 수동으로 등록합니다.")
    @PostMapping("/add")
    public ResponseEntity<Book> createBook(@RequestBody AddBookRequest addBookRequest) {
        Book savedBook = bookService.saveBook(addBookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    // 테스트용 수동 크롤링 호출 API
    @Operation(summary = "도서 크롤링 실행", description = "테스트용 수동 크롤링 API입니다.")
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
