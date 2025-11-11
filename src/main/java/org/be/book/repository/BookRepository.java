package org.be.book.repository;

import org.be.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByTitleAndAuthor(String title, String author);

    @Query("""
        SELECT DISTINCT b
        FROM Book b
        WHERE
            LOWER(b.title) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))
        """)
    List<Book> searchByKeyword(@Param("keyword") String keyword);
}
