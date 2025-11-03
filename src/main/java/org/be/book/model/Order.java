package org.be.book.model;

import jakarta.persistence.*;
import org.be.auth.model.User;
import java.time.LocalDateTime;

@Entity @Table(name = "orders")
public class Order {
    public enum Status { PENDING, PAID, FAILED, EXPIRED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.PENDING;

    private String pgProvider;
    private String pgOrderId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt = createdAt.plusMinutes(15);

    protected Order() {}
    public Order(User user, Book book, int quantity, long amount) {
        this.user = user; this.book = book; this.quantity = quantity; this.amount = amount;
    }

    public void markPaid(String provider, String txId) {
        this.status = Status.PAID;
        this.pgProvider = provider;
        this.pgOrderId = txId;
        this.paidAt = LocalDateTime.now();
    }
    public void markFailed() { this.status = Status.FAILED; }
    public void markExpired() { this.status = Status.EXPIRED; }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public int getQuantity() { return quantity; }
    public long getAmount() { return amount; }
    public Status getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}