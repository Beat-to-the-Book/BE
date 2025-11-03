package org.be.point.repository;

import java.util.Optional;
import org.be.auth.model.User;
import org.be.book.model.Book;
import org.be.point.model.UserBookRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookRecordRepository extends JpaRepository<UserBookRecord, Long> {
    Optional<UserBookRecord> findByUserAndBook(User user, Book book);
}