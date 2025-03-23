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

    public Book() {}

    public Book(String title, String author, String genre, Double price) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.price = price;
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
}
