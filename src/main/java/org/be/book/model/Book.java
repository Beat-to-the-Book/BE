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
    @Column(length = 500)
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

    @Column(nullable = false)
    private int rentalStock = 0;

    @Column(nullable = false)
    private int purchaseStock = 0;

    @Version
    private Long version;

    public Book() {}

    public Book(String title, String author,  double price, String genre,
                String publisher, String publishDate, String frontCoverImageUrl) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.genre = genre;
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

    public int getRentalStock() { return rentalStock; }
    public void setRentalStock(int rentalStock) { this.rentalStock = rentalStock; }

    public void decreaseRentalStock() {
        if (rentalStock <= 0) throw new IllegalStateException("대여 재고가 부족합니다.");
        rentalStock--;
    }

    public void increaseRentalStock() { rentalStock++; }

    public Long getVersion() { return version; }

    public int getPurchaseStock() { return purchaseStock; }
    public void setPurchaseStock(int purchaseStock) { this.purchaseStock = purchaseStock; }

    public void decreasePurchaseStock() { decreasePurchaseStock(1); }

    public void decreasePurchaseStock(int quantity) {
        if (quantity <= 0) quantity = 1;
        if (this.purchaseStock - quantity < 0) {
            throw new IllegalStateException("구매 재고가 부족합니다.");
        }
        this.purchaseStock -= quantity;
    }

    public void increasePurchaseStock() {
        increasePurchaseStock(1);
    }
    public void increasePurchaseStock(int quantity) {
        if (quantity <= 0) quantity = 1;
        this.purchaseStock += quantity;
    }
}