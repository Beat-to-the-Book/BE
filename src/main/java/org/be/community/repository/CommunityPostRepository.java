package org.be.community.repository;

import org.be.community.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user")
    List<CommunityPost> findAllWithUser();

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.id = :id")
    Optional<CommunityPost> findByIdWithUser(Long id);

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.group.id = :groupId")
    List<CommunityPost> findAllByGroupIdWithUser(@Param("groupId") Long groupId);
}
