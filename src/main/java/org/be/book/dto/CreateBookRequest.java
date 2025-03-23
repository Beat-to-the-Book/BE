package org.be.book.dto;

import lombok.Data;

@Data
public class CreateBookRequest {
    private String title;
    private String author;
    private String genre;
    private Double price;
}
