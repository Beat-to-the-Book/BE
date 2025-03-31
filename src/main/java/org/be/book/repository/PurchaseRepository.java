package org.be.book.repository;

import org.be.auth.model.User;
import org.be.book.model.Purchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    // 지연 로딩(Lazy Loading)을 피하고, book 엔티티를 함께 미리 로딩(Eager Loading)
    @EntityGraph(attributePaths = {"book"})
    List<Purchase> findByUser(User user);
}
