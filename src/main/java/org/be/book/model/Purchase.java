package org.be.book.model;

import jakarta.persistence.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase")
public class Purchase {

    public enum Status { PURCHASED, REFUND_REQUESTED }

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

    @Column(nullable = false)
    private int quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PURCHASED;

    private LocalDateTime refundRequestedAt;
    private String refundReason;

    public Purchase() {}

    public Purchase(User user, Book book) {
        this.user = user;
        this.book = book;
        this.purchaseDate = LocalDateTime.now();
    }

    public Purchase(User user, Book book, int quantity) {
        this(user, book);
        this.quantity = quantity;
    }

    public void requestRefund(String reason) {
        if (this.status != Status.PURCHASED) {
            throw new IllegalStateException("환불 신청이 불가능한 상태입니다.");
        }
        this.status = Status.REFUND_REQUESTED;
        this.refundRequestedAt = LocalDateTime.now();
        this.refundReason = reason;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public int getQuantity() { return quantity; }
    public Status getStatus() { return status; }
    public LocalDateTime getRefundRequestedAt() { return refundRequestedAt; }
    public String getRefundReason() { return refundReason; }
}
