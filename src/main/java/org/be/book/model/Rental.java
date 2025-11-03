package org.be.book.model;


import jakarta.persistence.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental")
public class Rental {
    public enum Status { RENTED, RETURNED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private LocalDateTime rentalDate = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dueDate = rentalDate.plusDays(14);

    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.RENTED;

    public Rental() {}

    public Rental(User user, Book book) {
        this.user = user;
        this.book = book;
        this.rentalDate = LocalDateTime.now();
        this.dueDate = this.rentalDate.plusDays(14);
        this.status = Status.RENTED;
    }

    public void returnBook() {
        if (this.status == Status.RETURNED) {
            throw new IllegalStateException("이미 반납된 대여건입니다.");
        }
        this.returnDate = LocalDateTime.now();
        this.status = Status.RETURNED;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDateTime getRentalDate() { return rentalDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public Status getStatus() { return status; }
}