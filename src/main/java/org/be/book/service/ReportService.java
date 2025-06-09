package org.be.book.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.be.auth.model.User;
import org.be.book.dto.ReportRequest;
import org.be.book.dto.ReportResponse;
import org.be.book.model.Book;
import org.be.book.model.Report;
import org.be.book.repository.BookRepository;
import org.be.book.repository.ReportRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BookRepository bookRepository;

    @Transactional
    public ReportResponse create(User user, ReportRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("해당 책이 없습니다."));

        Report report = new Report();
        report.setAuthor(user);
        report.setBook(book);
        report.setTitle(book.getTitle());
        report.setContent(request.getContent());
        report.setRating(request.getRating());
        report.setPublicVisible(request.getPublicVisible()); // ✅ 명확하게 값 전달

        log.info("📩 요청 받은 공개 여부: {}", request.getPublicVisible());
        log.info("🧪 before save - isPublic: {}", report.getPublicVisible());

        reportRepository.save(report);

        log.info("📦 after save - isPublic: {}", report.getPublicVisible());

        return ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getMyReports(User user) {
        return reportRepository.findByAuthor(user).stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportResponse get(Long id, User user) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 독후감입니다."));
        if (!report.getAuthor().equals(user)) {
            throw new AccessDeniedException("본인의 독후감만 조회할 수 있습니다.");
        }
        return ReportResponse.from(report);
    }

    @Transactional
    public void update(Long id, User user, ReportRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 독후감입니다."));
        if (!report.getAuthor().equals(user)) {
            throw new AccessDeniedException("본인의 독후감만 수정할 수 있습니다.");
        }

        report.setContent(request.getContent());
        report.setRating(request.getRating());
        report.setPublicVisible(request.getPublicVisible()); // ✅ 수정도 동일하게
    }

    @Transactional
    public void delete(Long id, User user) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 독후감입니다."));
        if (!report.getAuthor().equals(user)) {
            throw new AccessDeniedException("본인의 독후감만 삭제할 수 있습니다.");
        }

        reportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getPublicReportsByBook(Book book) {
        return reportRepository.findByBookAndPublicVisibleTrue(book).stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getAllPublicReports() {
        return reportRepository.findByPublicVisibleTrue().stream()
                .map(ReportResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportResponse getPublicReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 독후감입니다."));
        if (!Boolean.TRUE.equals(report.getPublicVisible())) {
            throw new AccessDeniedException("비공개 독후감은 조회할 수 없습니다.");
        }
        return ReportResponse.from(report);
    }
}