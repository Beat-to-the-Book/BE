package org.be.book.model;


import jakarta.persistence.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental")
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;

    private LocalDateTime rentalDate = LocalDateTime.now();
    private LocalDateTime returnDate;

    public Rental() {}

    public Rental(User user, Book book) {
        this.user = user;
        this.book = book;
        this.rentalDate = LocalDateTime.now();
    }

    public void returnBook() {
        this.returnDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDateTime getRentalDate() { return rentalDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
}
