package org.be.book.model;

import jakarta.persistence.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
//    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private LocalDateTime purchaseDate = LocalDateTime.now();

    public Purchase() {}

    public Purchase(User user, Book book) {
        this.user = user;
        this.book = book;
        this.purchaseDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
}
