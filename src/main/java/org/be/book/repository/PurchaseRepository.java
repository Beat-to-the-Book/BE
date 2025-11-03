package org.be.book.repository;

import org.be.auth.model.User;
import org.be.book.model.Purchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @EntityGraph(attributePaths = {"book"})
    List<Purchase> findByUser(User user);
    List<Purchase> findByUserAndStatus(User user, Purchase.Status status);
}
