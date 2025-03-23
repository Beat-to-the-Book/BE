package org.be.action.repository;

import org.be.action.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {

    List<Action> findByUserId(Long userId);
    List<Action> findByBookId(Long bookId);
    List<Action> findByUserIdAndActionType(Long userId, Action.ActionType actionType);
}
