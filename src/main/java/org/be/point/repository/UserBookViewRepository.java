package org.be.point.repository;

import java.util.List;

import org.be.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBookViewRepository extends JpaRepository<Book, Long> {

    interface Row {
        Long getBookId();
        String getTitle();
        String getLeftCoverImageUrl();
        String getFrontCoverImageUrl();
        String getBackCoverImageUrl();
        Integer getPurchased();
        Integer getRented();
        Integer getThrown();
    }

    @Query(value = """
    SELECT 
           b.id    AS bookId,
           b.title AS title,
           b.left_cover_image_url  AS leftCoverImageUrl,
           b.front_cover_image_url AS frontCoverImageUrl,
           b.back_cover_image_url  AS backCoverImageUrl,
           CASE WHEN EXISTS(SELECT 1 
                           FROM purchase p 
                           WHERE p.user_id = :userPk AND p.book_id = b.id)
                THEN 1 ELSE 0 END AS purchased,
           CASE WHEN EXISTS(SELECT 1 
                           FROM rental r 
                           WHERE r.user_id = :userPk AND r.book_id = b.id)
                THEN 1 ELSE 0 END AS rented,
           COALESCE(ubr.thrown, 0) AS thrown
    FROM (
        SELECT DISTINCT book_id FROM purchase WHERE user_id = :userPk
        UNION
        SELECT DISTINCT book_id FROM rental   WHERE user_id = :userPk
    ) ub
    JOIN books b 
      ON b.id = ub.book_id
    LEFT JOIN user_book_record ubr
           ON ubr.user_id = :userPk 
          AND ubr.book_id = ub.book_id
    ORDER BY b.id DESC
    """, nativeQuery = true)
    List<Row> findUserBooksWithFlags(@Param("userPk") Long userPk);
}