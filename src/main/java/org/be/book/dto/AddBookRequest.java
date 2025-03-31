package org.be.book.dto;

import lombok.Data;

@Data
public class AddBookRequest {
    private String title;
    private String author;
    private String genre;
    private Double price;
    private String publisher;
    private String publishedDate;
    private String leftCoverImageUrl;
    private String frontCoverImageUrl;
    private String backCoverImageUrl;
}
