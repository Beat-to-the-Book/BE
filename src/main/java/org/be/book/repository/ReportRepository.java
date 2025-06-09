package org.be.book.repository;

import org.be.auth.model.User;
import org.be.book.model.Book;
import org.be.book.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByAuthor(User user);
    List<Report> findByBookAndPublicVisibleTrue(Book book);
    List<Report> findByPublicVisibleTrue();
}