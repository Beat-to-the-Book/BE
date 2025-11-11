package org.be.bookshelf.repository;

import org.be.auth.model.User;
import org.be.bookshelf.model.Bookshelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {
    Optional<Bookshelf> findByUser(User user);
}