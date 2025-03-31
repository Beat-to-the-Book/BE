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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        String url = "https://www.aladin.co.kr/shop/common/wbest.aspx?BranchType=1&start=we";

        try {
            driver.get(url);
            Thread.sleep(5000);

            String html = driver.getPageSource();
            Document doc = Jsoup.parse(html);

            // 베스트셀러 목록 (예: .ss_book_list:nth-child(2n+1))
            Elements booksList = doc.select(".ss_book_list:nth-child(2n+1)");
            log.info("🔍 크롤링된 책 개수: {}", booksList.size());

            List<Book> books = new ArrayList<>();

            for (Element bookElement : booksList) {
                Elements liElements = bookElement.select("li");
                if (liElements.size() < 4) continue;

                // (1) 리스트 페이지에서 제목 추출
                Element titleElement = liElements.get(1).selectFirst(".bo3");
                if (titleElement == null) {
                    log.warn("제목을 찾지 못했습니다. li 요소: {}", liElements.get(1).html());
                    continue;
                }
                String title = titleElement.text().trim();

                // (2) 리스트 페이지에서 작가, 출판사, 발행일 추출
                String[] bookInfo = liElements.get(2).text().split("\\|");
                String author = bookInfo[0].trim();
                String publisher = (bookInfo.length > 1) ? bookInfo[1].trim() : "출판사 없음";
                String rawPublishDate = (bookInfo.length > 2) ? bookInfo[2].trim() : "발행일 없음";
                String publishDate = rawPublishDate;
                Pattern datePattern = Pattern.compile("(\\d+)년\\s*(\\d+)월\\s*(\\d+)일");
                Matcher matcher = datePattern.matcher(rawPublishDate);
                if (matcher.find()) {
                    publishDate = matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3);
                }

                // (3) 리스트 페이지에서 가격 추출
                Element priceEl = liElements.get(3).selectFirst("span.ss_p2");
                String priceText = (priceEl != null) ? priceEl.text() : "";
                String numericPrice = priceText.replaceAll("[^0-9]", "");
                Double price = numericPrice.isEmpty() ? 0.0 : Double.parseDouble(numericPrice);

                // (4) 리스트 페이지에서 장르 추출
                Element genreEl = bookElement.selectFirst("span.tit_category");
                String rawGenre = (genreEl != null) ? genreEl.text() : "";
                String genre = rawGenre.replace("[", "").replace("]", "").trim();
                if (genre.isEmpty()) {
                    genre = "장르 없음";
                }

                // (5) 리스트 페이지에서 상세 페이지 링크 추출 (bo3 안의 <a> 태그)
                Element detailLinkEl = titleElement.selectFirst("a");
                if (detailLinkEl == null) {
                    log.warn("상세 링크를 찾지 못했습니다. 제목: {}", title);
                    continue;
                }
                String detailUrl = detailLinkEl.absUrl("href");

                // (6) 상세 페이지에서 커버 이미지(앞, 뒤, 왼쪽) 추출
                String frontCoverImageUrlDetail = "이미지 없음";
                String backCoverImageUrlDetail = "이미지 없음";
                String leftCoverImageUrlDetail = "이미지 없음";

                WebDriver detailDriver = new ChromeDriver(options);
                try {
                    detailDriver.get(detailUrl);
                    WebDriverWait wait = new WebDriverWait(detailDriver, Duration.ofSeconds(10));


                    //(이건 주제 분륜데,,,)
//                    try {
//                        WebElement descElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
//                                By.cssSelector("div#Ere_prod_allwrap > div.Ere_prod_middlewrap > div.Ere_prod_mconts_box > div.Ere_prod_mconts_R")
//                        ));
//                        bookDescription = descElement.getText().trim();
//                    } catch (TimeoutException e) {
//                        log.warn("책 소개 로딩 실패");
//                    }
//                    log.info("책 소개: {}", bookDescription);


                    // 앞 커버 이미지 추출
                    try {
                        WebElement frontCoverEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.id("CoverMainImage")
                        ));
                        frontCoverImageUrlDetail = frontCoverEl.getAttribute("src");
                    } catch (TimeoutException e) {
                        log.warn("앞 커버 이미지를 찾지 못했습니다.");
                    }

                    // 뒤 커버 이미지 추출
                    try {
                        WebElement backCoverEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("div.c_back img")
                        ));
                        backCoverImageUrlDetail = backCoverEl.getAttribute("src");
                    } catch (TimeoutException e) {
                        log.warn("뒤 커버 이미지를 찾지 못했습니다.");
                    }

                    // 왼쪽 커버 이미지 추출
                    try {
                        WebElement leftCoverEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("div.c_left img")
                        ));
                        leftCoverImageUrlDetail = leftCoverEl.getAttribute("src");
                    } catch (TimeoutException e) {
                        log.warn("왼쪽 커버 이미지를 찾지 못했습니다.");
                    }
                } finally {
                    detailDriver.quit();
                }

                log.info("📚 제목: {}", title);
                log.info("✍️ 작가: {}", author);
                log.info("🏢 출판사: {}", publisher);
                log.info("🗓️ 발행일: {}", publishDate);
                log.info("💰 가격: {}원", price);
                log.info("📖 장르: {}", genre);
                log.info("🔗 상세 페이지 링크: {}", detailUrl);
                log.info("🖼️ 상세페이지 앞 커버 이미지: {}", frontCoverImageUrlDetail);
                log.info("🖼️ 상세페이지 뒤 커버 이미지: {}", backCoverImageUrlDetail);
                log.info("🖼️ 상세페이지 왼쪽 커버 이미지: {}", leftCoverImageUrlDetail);
                log.info("----------------------------------");

                Book book = new Book(
                        title,
                        author,
                        price,
                        publisher,
                        publishDate,
                        frontCoverImageUrlDetail // 대표 이미지로 앞 커버 사용
                );
                book.setFrontCoverImageUrl(frontCoverImageUrlDetail);
                book.setBackCoverImageUrl(backCoverImageUrlDetail);
                book.setLeftCoverImageUrl(leftCoverImageUrlDetail);
                book.setGenre(genre);

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