package org.be.book.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.be.auth.service.CustomUserDetails;
import org.be.book.dto.ReportRequest;
import org.be.book.dto.ReportResponse;
import org.be.book.model.Book;
import org.be.book.repository.BookRepository;
import org.be.book.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final BookRepository bookRepository;

    private void validateAuth(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
    }

    // 1. 독후감 작성
    @PostMapping("/me")
    public ResponseEntity<ReportResponse> writeReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @RequestBody ReportRequest request) {
        validateAuth(userDetails);
        ReportResponse response = reportService.create(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }

    // 2. 내 독후감 전체 조회
    @GetMapping("/me")
    public ResponseEntity<List<ReportResponse>> getMyReports(@AuthenticationPrincipal CustomUserDetails userDetails) {
        validateAuth(userDetails);
        return ResponseEntity.ok(reportService.getMyReports(userDetails.getUser()));
    }

    // 3. 내 독후감 단건 조회
    @GetMapping("/me/{id}")
    public ResponseEntity<ReportResponse> getReport(@PathVariable Long id,
                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(reportService.get(id, userDetails.getUser()));
    }

    // 4. 수정
    @PutMapping("/me/{id}")
    public ResponseEntity<Void> updateReport(@PathVariable Long id,
                                             @RequestBody ReportRequest request,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reportService.update(id, userDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }

    // 5. 삭제
    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reportService.delete(id, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    // 6. 특정 책에 대한 공개 독후감 조회
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReportResponse>> getReportsByBook(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다."));
        return ResponseEntity.ok(reportService.getPublicReportsByBook(book));
    }

    // 7. 전체 공개 독후감 목록
    @GetMapping("/public")
    public ResponseEntity<List<ReportResponse>> getPublicReports() {
        return ResponseEntity.ok(reportService.getAllPublicReports());
    }

    // 8. 공개된 단건 독후감
    @GetMapping("/public/{id}")
    public ResponseEntity<ReportResponse> getPublicReport(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getPublicReport(id));
    }
}