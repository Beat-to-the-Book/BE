package org.be.book.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 255)
    private String title;

    @NotBlank
    @Column(length = 100)
    private String author;

    @NotBlank
    @Column(length = 50)
    private String genre;

    @NotNull
    @Column(nullable = false)
    private Double price;

    @Column(length = 100)
    private String publisher;

    @Column(name = "publish_date")
    private String publishDate;

    @Column(length = 500)
    private String leftCoverImageUrl;

    @Column(length = 500)
    private String frontCoverImageUrl;

    @Column(length = 500)
    private String backCoverImageUrl;

    public Book() {}

    public Book(String title, String author,  double price,
                String publisher, String publishDate, String frontCoverImageUrl) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.frontCoverImageUrl = frontCoverImageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getLeftCoverImageUrl() { return leftCoverImageUrl; }
    public void setLeftCoverImageUrl(String leftCoverImageUrl) { this.leftCoverImageUrl = leftCoverImageUrl; }

    public String getFrontCoverImageUrl() { return frontCoverImageUrl; }
    public void setFrontCoverImageUrl(String frontCoverImageUrl) { this.frontCoverImageUrl = frontCoverImageUrl; }

    public String getBackCoverImageUrl() { return backCoverImageUrl; }
    public void setBackCoverImageUrl(String backCoverImageUrl) { this.backCoverImageUrl = backCoverImageUrl; }
}