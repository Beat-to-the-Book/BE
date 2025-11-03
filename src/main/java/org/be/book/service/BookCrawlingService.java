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
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookCrawlingService {

    private final BookRepository bookRepository;

    public void crawlBooks() throws Exception {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        List<String> categoryUrls = List.of(
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=170",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=2105",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=987",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=8257",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=2551",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=798",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=1",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=1108",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=55889",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=1196",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=74",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=656",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=336",
                "https://www.aladin.co.kr/shop/common/wbest.aspx?BestType=Bestseller&BranchType=1&CID=351"
        );

        try {
            for(String baseUrl : categoryUrls) {
                int page = 1;
                while (true) {
                    String url = baseUrl + "&page=" + page;
                    log.info("🔍 카테고리 URL: {}", url);

                    driver.get(url);
                    Thread.sleep(5000);

                    String html = driver.getPageSource();
                    Document doc = Jsoup.parse(html);

                    // 베스트셀러 목록
                    Elements booksList = doc.select(".ss_book_list:nth-child(2n+1)");
                    if (booksList.isEmpty()) {
                        log.info("📄 마지막 페이지 도달: {}", page);
                        break;
                    }

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

                        // 19세 이상 도서 필터링
                        Element imgTag = liElements.get(0).selectFirst("img");
                        if (imgTag != null && imgTag.attr("src").contains("19book")) {
                            log.info("🚫 19세 이상 도서 제외됨: {}", title);
                            continue;
                        }

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
                        if (price == 0.0) {
                            log.info("💸 가격이 0원인 책 제외: {}", title);
                            continue;
                        }

                        // (4) 리스트 페이지에서 상세 페이지 링크 추출 (bo3 안의 <a> 태그)
                        Element detailLinkEl = titleElement.selectFirst("a");
                        if (detailLinkEl == null) {
                            log.warn("상세 링크를 찾지 못했습니다. 제목: {}", title);
                            continue;
                        }
                        String detailUrl = detailLinkEl.absUrl("href");

                        // (5) 상세 페이지에서 커버 이미지(앞, 뒤, 왼쪽) 추출
                        String frontCoverImageUrlDetail = "http://localhost:8082/images/default_cover.jpg";
                        String backCoverImageUrlDetail = "http://localhost:8082/images/default_cover.jpg";
                        String leftCoverImageUrlDetail = "http://localhost:8082/images/default_cover.jpg";
                        String genre = "장르 없음";

                        WebDriver detailDriver = new ChromeDriver(options);
                        try {
                            detailDriver.get(detailUrl);

                            try {
                                Alert alert = detailDriver.switchTo().alert();
                                String alertText = alert.getText();
                                if (alertText.contains("19세") || alertText.contains("로그인")) {
                                    log.info("🚫 상세 페이지 alert 감지됨 (19세 도서): {}", alertText);
                                    alert.dismiss();
                                    continue;
                                }
                            } catch (NoAlertPresentException e) {}

                            WebDriverWait wait = new WebDriverWait(detailDriver, Duration.ofSeconds(10));

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

                            // (6) 장르 추출
                            try {
                                WebElement descElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                        By.cssSelector("div#Ere_prod_allwrap > div.Ere_prod_middlewrap > div.Ere_prod_mconts_box > div.Ere_prod_mconts_R > div.conts_info_list2")
                                ));
                                String fullGenre = descElement.getText().trim();
                                String[] genreParts = fullGenre.split(">");

                                if (genreParts.length >= 2) {
                                    genre = genreParts[1].trim();
                                } else if (genreParts.length >= 1 && !genreParts[0].trim().isEmpty()) {
                                    genre = genreParts[0].trim();
                                } else {
                                    genre = "기타";
                                }
                                if (genre.trim().isEmpty()) {
                                    genre = "기타";
                                }
                            } catch (TimeoutException e) {
                                log.warn("책 장르 로딩 실패");
                                genre = "기타";
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
                                genre,
                                publisher,
                                publishDate,
                                frontCoverImageUrlDetail // 대표 이미지로 앞 커버 사용
                        );
                        book.setFrontCoverImageUrl(frontCoverImageUrlDetail);
                        book.setBackCoverImageUrl(backCoverImageUrlDetail);
                        book.setLeftCoverImageUrl(leftCoverImageUrlDetail);

                        // 초기 대여 재고를 2~10 사이 랜덤으로 세팅
                        int randomStock = ThreadLocalRandom.current().nextInt(2, 11);
                        book.setRentalStock(randomStock);
                        log.info("📦 초기 대여 재고: {}", randomStock);

                        // 초기 구매 재고를 2~20 사이 랜덤으로 세팅
                        int randomPurchaseStock = ThreadLocalRandom.current().nextInt(2, 21);
                        book.setPurchaseStock(randomPurchaseStock);
                        log.info("🛒 초기 구매 재고: {}", randomPurchaseStock);


                        // 중복 책 저장 방지
                        if (bookRepository.existsByTitleAndAuthor(title, author)) {
                            log.info("⚠️ 이미 존재하는 책: {} - {}, 저장하지 않음", title, author);
                            continue;
                        }

                        try {
                            bookRepository.save(book);
                        } catch (Exception e) {
                            log.error("❌ 책 저장 실패: {}", e.getMessage());
                        }
                        books.add(book);
                    }

                    log.info("✅ {}페이지 - 총 {}권의 책을 저장했습니다.", page, books.size());
                    page++;
                }
            }
        }finally {
            driver.quit();
        }
    }
}