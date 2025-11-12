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
import org.be.book.model.BookDetail;
import org.be.book.repository.BookDetailRepository;
import org.be.book.service.BookDetailCrawlingService;

@Tag(name = "도서 API", description = "도서 검색, 등록, 크롤링 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final BookCrawlingService bookCrawlingService;
    private final BookDetailCrawlingService bookDetailCrawlingService;
    private final BookDetailRepository bookDetailRepository;

    // 모든 도서 조회
    @Operation(summary = "모든 도서 목록 조회", description = "등록된 모든 도서를 조회합니다.")
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // 도서 한 권 조회
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // 도서 검색
    @Operation(summary = "도서 검색", description = "도서 제목과 저자에 키워드가 포함된 도서를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(bookService.searchBooks(keyword));
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

    @Operation(summary = "도서 상세 정보 조회", description = "bookId 기준으로 저장된 상세 정보를 조회합니다. 없으면 404를 반환합니다.")
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getBookDetail(@PathVariable Long id) {
        return bookDetailRepository.findByBookId(id)
                .map(d -> new org.be.book.dto.BookDetailResponse(
                        d.getId(),
                        d.getBook().getId(),
                        null,
                        d.getEditorPickHtml(),
                        d.getIntroductionHtml(),
                        d.getTocHtml(),
                        d.getSourceUrl()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "도서 상세 정보 크롤링/업서트", description = "상세페이지 URL을 받아 해당 도서의 상세 정보를 크롤링하여 저장/업데이트합니다.")
    @PostMapping("/{id}/crawl-detail")
    public ResponseEntity<String> crawlBookDetail(
            @PathVariable Long id,
            @RequestParam(name = "url", required = false) String detailUrl
    ) {
        if (detailUrl == null || detailUrl.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("detail URL이 필요합니다. (query param: url)");
        }
        try {
            bookDetailCrawlingService.crawlAndUpsertDetails(id, detailUrl);
            return ResponseEntity.ok("상세 크롤링/업서트 완료");
        } catch (Exception e) {
            log.error("상세 크롤링 중 에러 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("상세 크롤링 실패");
        }
    }
}
