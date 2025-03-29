package org.be.book.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.be.book.model.Book;
import org.be.book.repository.BookRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCrawlingService {
    private final BookRepository bookRepository;

    public void crawlBooks() throws Exception {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        String url = "https://www.aladin.co.kr/shop/common/wbest.aspx?BranchType=1&start=we";

        try {
            driver.get(url);
            Thread.sleep(5000);  // 페이지 로딩 대기

            String html = driver.getPageSource();
            Document doc = Jsoup.parse(html);
            Elements booksList = doc.select(".ss_book_list:nth-child(2n+1)");

            List<Book> books = new ArrayList<>();
            log.info("🔍 크롤링된 책 개수: {}", booksList.size());

            for (Element bookElement : booksList) {
                Elements liElements = bookElement.select("li");
                if (liElements.size() < 4) continue;

                // 제목 크롤링
                Element titleElement = liElements.get(1).selectFirst(".bo3");
                if (titleElement == null) {
                    log.warn("제목을 찾지 못했습니다. 해당 li 요소: {}", liElements.get(1).html());
                    continue; // 또는 기본값을 할당하는 방식도 고려할 수 있습니다.
                }
                String title = titleElement.text().trim();

                // 책 설명 (내용)
                String bookDescription = liElements.get(1).selectFirst(".ss_f_g2") != null ?
                        liElements.get(1).selectFirst(".ss_f_g2").text() : "설명 없음";

                // 작가, 출판사, 발행일
                String[] bookInfo = liElements.get(2).text().split("\\|");
                String author = bookInfo[0].trim();
                String publisher = bookInfo.length > 1 ? bookInfo[1].trim() : "출판사 없음";
                String rawPublishDate = bookInfo.length > 2 ? bookInfo[2].trim() : "발행일 없음";
                String publishDate = rawPublishDate;
                // "YYYY년 MM월 DD일" 형식을 "YYYY-MM-DD"로 변환
                Pattern pattern = Pattern.compile("(\\d+)년\\s*(\\d+)월\\s*(\\d+)일");
                Matcher matcher = pattern.matcher(rawPublishDate);
                if (matcher.find()) {
                    publishDate = matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3);
                }

                // 가격 정보 추출 (네 번째 li 요소)
                String[] priceInfo = liElements.get(3).text().split("→");
                String originalPriceStr = priceInfo[0].replaceAll("[^0-9]", "").trim();
                String discountedPriceStr = priceInfo.length > 1 ? priceInfo[1].replaceAll("[^0-9]", "").trim() : originalPriceStr;
                Double price = 0.0;
                if (!discountedPriceStr.isEmpty()) {
                    price = Double.parseDouble(discountedPriceStr);
                }

                // 커버 이미지 URL 크롤링
                Element coverImgEl = liElements.get(0).selectFirst("img");
                String coverImageUrl = coverImgEl != null ? coverImgEl.attr("src") : "이미지 없음";

                log.info("📚 제목: {}", title);
                log.info("✍️ 작가: {}", author);
                log.info("🏢 출판사: {}", publisher);
                log.info("🗓️ 발행일: {}", publishDate);
                log.info("💰 가격: {}원", price);
                log.info("🖼️ 커버 이미지: {}", coverImageUrl);
                log.info("🏷️ 내용: {}", bookDescription);
                log.info("----------------------------------");

                // 가격은 필요하지 않으므로 기본값 0.0 사용
                Book book = new Book(title, author, bookDescription, 0.0, publisher, publishDate, coverImageUrl);
                // 장르 정보 설정 (예시로 "소설"로 고정)
                book.setGenre("소설");

                try {
                    bookRepository.save(book);
                } catch (Exception e) {
                    log.error("❌ 책 저장 실패: {}", e.getMessage());
                }

                books.add(book);
            }

            log.info("✅ 총 {}권의 책을 저장했습니다.", books.size());
        } finally {
            driver.quit();
        }
    }
}