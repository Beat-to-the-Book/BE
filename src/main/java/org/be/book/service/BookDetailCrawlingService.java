package org.be.book.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.be.book.dto.BookDetailData;
import org.be.book.model.Book;
import org.be.book.model.BookDetail;
import org.be.book.repository.BookRepository;
import org.be.book.repository.BookDetailRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookDetailCrawlingService {

    private final BookRepository bookRepository;
    private final BookDetailRepository bookDetailRepository;

    /** 상세 페이지에서 '책소개'와 '목차'만 파싱 */
    public BookDetailData crawlDetail(String detailUrl) {
        try {
            Document doc = Jsoup.connect(detailUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                    .referrer("https://www.aladin.co.kr/")
                    .timeout(15000)
                    .get();

            String introduction = extractSection(doc, "책소개");
            String toc = extractSection(doc, "목차");

            // 편집장의 선택 / 기본정보는 사용하지 않으므로 빈 값으로 반환
            return new BookDetailData("", introduction, toc, "");

        } catch (Exception e) {
            log.error("❌ 상세 페이지 크롤링 실패: {}", e.getMessage());
            return null;
        }
    }

    /** 파싱 결과를 BookDetail에 업서트. (책소개/목차만 갱신, editor_pick 등은 건드리지 않음) */
    @Transactional
    public void crawlAndUpsertDetails(Long bookId, String detailUrl) {
        if (detailUrl == null || detailUrl.isBlank()) {
            log.warn("상세 URL이 비어있어 상세 크롤링을 건너뜁니다. bookId={}", bookId);
            return;
        }

        BookDetailData data = crawlDetail(detailUrl);
        if (data == null) {
            log.warn("상세 크롤링 결과가 없어 업서트를 건너뜁니다. bookId={}, url={}", bookId, detailUrl);
            return;
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("도서를 찾을 수 없습니다. bookId=" + bookId));

        Optional<BookDetail> opt = bookDetailRepository.findByBook(book);
        BookDetail detail = opt.orElseGet(() -> BookDetail.builder().book(book).build());

        // ✅ editorPickHtml은 덮어쓰지 않음
        if (notBlank(data.getIntroduction())) {
            detail.setIntroductionHtml(data.getIntroduction());
        }
        if (notBlank(data.getToc())) {
            detail.setTocHtml(data.getToc());
        }
        detail.setSourceUrl(detailUrl);

        bookDetailRepository.save(detail);
        log.info("✅ BookDetail 업서트 완료(책소개/목차만 갱신): bookId={}, url={}", bookId, detailUrl);
    }

    /** 공통 섹션 파서: 라벨(LL/LS) 오른쪽 본문 + 줄바꿈 보존 */
    private String extractSection(Document doc, String title) {
        String titleRegex = switch (title) {
            case "책소개" -> "(?i)책\\s*소개";
            case "목차"   -> "(?i)목\\s*차";
            default -> title;
        };

        var box = doc.selectFirst(
                ".Ere_prod_mconts_box:has(.Ere_prod_mconts_LL:matchesOwn(" + titleRegex + ")),"
                        + ".Ere_prod_mconts_box:has(.Ere_prod_mconts_LS:matchesOwn(" + titleRegex + "))"
        );
        var target = (box != null) ? box.selectFirst(".Ere_prod_mconts_R") : null;
        if (target == null) return "";

        String html = target.html();
        String withBreaks = html.replaceAll("(?i)<br\\s*/?>", "\n");
        String plain = withBreaks.replaceAll("(?s)<[^>]+>", "").trim();
        return plain;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}