package org.be.book.repository;

import org.be.book.model.Book;
import org.be.book.model.Review;
import org.be.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookAndAuthor(Book book, User user);
    List<Review> findAllByBook(Book book);
}