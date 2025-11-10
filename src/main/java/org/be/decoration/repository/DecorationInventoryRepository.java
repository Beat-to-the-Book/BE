// org/be/decoration/repository/DecorationInventoryRepository.java
package org.be.decoration.repository;

import java.util.Optional;
import org.be.auth.model.User;
import org.be.decoration.model.DecorationInventory;
import org.be.decoration.model.DecorationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecorationInventoryRepository extends JpaRepository<DecorationInventory, Long> {
    Optional<DecorationInventory> findByUserAndType(User user, DecorationType type);
}