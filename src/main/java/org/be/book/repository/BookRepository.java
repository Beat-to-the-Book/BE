package org.be.book.repository;

import org.be.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findAllByTitleContaining(String keyword);
    boolean existsByTitleAndAuthor(String title, String author);
}
