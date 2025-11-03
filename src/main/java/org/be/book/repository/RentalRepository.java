package org.be.book.repository;

import org.be.auth.model.User;
import org.be.book.model.Book;
import org.be.book.model.Rental;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    @EntityGraph(attributePaths = {"book"})
    List<Rental> findByUser(User user);
    List<Rental> findByUserAndStatus(User user, Rental.Status status);
    boolean existsByUserAndBookAndStatus(User user, Book book, Rental.Status status);
}