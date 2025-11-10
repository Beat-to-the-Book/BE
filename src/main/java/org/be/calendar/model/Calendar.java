package org.be.calendar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.be.auth.model.User;
import org.be.book.model.Book;

import java.time.LocalDate;

@Entity
@Table(name = "calendar_entries")
@Getter
@NoArgsConstructor
public class Calendar {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 관련 도서
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public Calendar(User user, Book book, LocalDate startDate, LocalDate endDate, String memo) {
        this.user = user;
        this.book = book;
        this.startDate = startDate;
        this.endDate = endDate;
        this.memo = memo;
    }

    public void update(LocalDate startDate, LocalDate endDate, String memo, Book book) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.memo = memo;
        if (book != null) this.book = book;
    }
}