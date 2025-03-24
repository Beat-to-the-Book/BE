package org.be.book.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.be.auth.model.User;

@Entity
@Table(name = "ratings")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull
    @Column(nullable = false)
    private Integer score; // 1~5점 평점

    public Rating() {}

    public Rating(User user, Book book, Integer score) {
        this.user = user;
        this.book = book;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
}