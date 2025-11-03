package org.be.point.model;

import jakarta.persistence.*;
import org.be.auth.model.User;
import org.be.book.model.Book;

@Entity
@Table(
        name = "user_book_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
public class UserBookRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "thrown", nullable = false)
    private boolean thrown = false;

    protected UserBookRecord() {}

    public UserBookRecord(User user, Book book) {
        this.user = user;
        this.book = book;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Book getBook() { return book; }
    public boolean isThrown() { return thrown; }
    public void markThrown() { this.thrown = true; }
}