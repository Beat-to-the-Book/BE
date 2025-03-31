package org.be.book.dto;

import lombok.Data;

@Data
public class AddBookRequest {
    private String title;
    private String author;
    private String genre;
    private Double price;
    private String bookDescription;
    private String publisher;
    private String publishedDate;
    private String coverImage;
}
