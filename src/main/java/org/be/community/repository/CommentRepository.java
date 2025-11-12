package org.be.community.repository;

import org.be.community.entity.Comment;
import org.be.community.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostOrderByRootIdAscCreatedAtAsc(CommunityPost post);

    List<Comment> findByPostAndDepthOrderByCreatedAtAsc(CommunityPost post, byte depth);
}