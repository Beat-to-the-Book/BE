package org.be.book.repository;

import org.be.book.model.Book;
import org.be.book.model.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookDetailRepository extends JpaRepository<BookDetail, Long> {
    Optional<BookDetail> findByBookId(Long bookId);
    boolean existsByBookId(Long bookId);
    Optional<BookDetail> findByBook(Book book);
}
