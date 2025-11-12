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

    public BookDetailData crawlDetail(String detailUrl) {
        try {
            Document doc = Jsoup.connect(detailUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 편집장의 선택: 보다 관대한 전용 파서 사용, 그래도 비면 기존 섹션 파서로 한 번 더 시도
            String editorsPick = extractEditorsPick(doc);
            if (editorsPick == null || editorsPick.isBlank()) {
                editorsPick = extractSection(doc, "편집장의 선택");
            }

            String introduction = extractSection(doc, "책소개");
            String toc = extractSection(doc, "목차");
            String basicInfo = extractBasicInfo(doc);

            return new BookDetailData(editorsPick, introduction, toc, basicInfo);

        } catch (Exception e) {
            log.error("❌ 상세 페이지 크롤링 실패: {}", e.getMessage());
            return null;
        }
    }

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

        // 현재 파싱이 완료된 항목들만 반영 (기존 로직 유지, 필요한 최소 추가만)
        detail.setEditorPickHtml(safe(data.getEditorsPick()));
        detail.setIntroductionHtml(safe(data.getIntroduction()));
        detail.setTocHtml(safe(data.getToc()));
        detail.setSourceUrl(detailUrl);

        bookDetailRepository.save(detail);
        log.info("✅ BookDetail 업서트 완료: bookId={}, url={}", bookId, detailUrl);
    }

    private String extractEditorsPick(Document doc) {
        // 라벨이 LL 또는 LS 인 케이스 모두 허용, containsOwn으로 완화
        var box = doc.selectFirst(
                ".Ere_prod_mconts_box:has(.Ere_prod_mconts_LL:containsOwn(편집장의 선택))," +
                ".Ere_prod_mconts_box:has(.Ere_prod_mconts_LS:containsOwn(편집장의 선택))"
        );
        if (box == null) return "";

        var right = box.selectFirst(".Ere_prod_mconts_R");
        if (right == null) return "";

        // MD 문구/라인 구분 등 부가 요소 제거
        right.select(".Ere_sub_blue, .Ere_line2, .Ere_space10").remove();

        // <br>는 개행으로 보존 후 텍스트만 추출
        String html = right.html()
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n");
        String text = Jsoup.parse(html).text();
        return text.replaceAll("\\s*\\n\\s*", "\n").trim();
    }

    private String extractSection(Document doc, String title) {
        // 라벨 표기가 페이지마다 약간씩 달라지는 것을 허용
        String titleRegex = switch (title) {
            case "책소개" -> "(?i)책\\s*소개";
            case "편집장의 선택" -> "(?i)편집장(?:의)?\\s*선택";
            case "목차" -> "(?i)목\\s*차";
            default -> title;
        };

        // 1) 기본 셀렉터: 라벨(LL/LS)을 갖는 박스의 오른쪽 영역
        var box = doc.selectFirst(
                ".Ere_prod_mconts_box:has(.Ere_prod_mconts_LL:matchesOwn(" + titleRegex + ")),"
              + ".Ere_prod_mconts_box:has(.Ere_prod_mconts_LS:matchesOwn(" + titleRegex + "))"
        );
        var target = (box != null) ? box.selectFirst(".Ere_prod_mconts_R") : null;

        // 2) 기본 셀렉터로 내용이 비었으면 보정(fallback)
        if ((target == null || target.text().isBlank()) && "편집장의 선택".equals(title)) {
            // 에디터픽 전용: 타이틀(H2 비슷한) .Ere_fs18 블록 또는 MD 서명(.Ere_sub_blue ...)을 포함한 영역을 기준으로 탐색
            target = doc.selectFirst(".Ere_prod_mconts_R:has(.Ere_fs18)");
            if (target == null) {
                target = doc.selectFirst(".Ere_prod_mconts_box .Ere_prod_mconts_R:has(.Ere_sub_blue)");
            }
        }

        if (target == null) return "";

        // 줄바꿈 보존: <br> -> 개행 후 나머지 태그 제거
        String html = target.html();
        String withBreaks = html.replaceAll("(?i)<br\\s*/?>", "\n");
        String plain = withBreaks.replaceAll("(?s)<[^>]+>", "").trim();
        return plain;
    }

    private String extractBasicInfo(Document doc) {
        return doc.select("div.conts_info_list1 ul").text();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}