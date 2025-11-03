package org.be.book.repository;

import org.be.auth.model.User;
import org.be.book.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUser(Long id, User user);
}