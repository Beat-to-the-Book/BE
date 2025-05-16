package org.be.book.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "도서 등록 요청 DTO")
@Data
public class AddBookRequest {
    @Schema(description = "도서 제목", example = "자바의 정석")
    private String title;

    @Schema(description = "저자", example = "남궁성")
    private String author;

    @Schema(description = "장르", example = "프로그래밍")
    private String genre;

    @Schema(description = "가격", example = "30000")
    private Double price;

    @Schema(description = "출판사", example = "도우출판")
    private String publisher;

    @Schema(description = "출판일 (YYYY-MM-DD)", example = "2022-01-15")
    private String publishedDate;

    @Schema(description = "책 왼쪽 표지 이미지 URL", example = "https://example.com/images/book-left.jpg")
    private String leftCoverImageUrl;

    @Schema(description = "책 정면 표지 이미지 URL", example = "https://example.com/images/book-front.jpg")
    private String frontCoverImageUrl;

    @Schema(description = "책 뒷면 표지 이미지 URL", example = "https://example.com/images/book-back.jpg")
    private String backCoverImageUrl;
}
