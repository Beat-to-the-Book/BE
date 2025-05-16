package org.be.group.repository;

import org.be.group.entity.UserGroup;
import org.be.group.entity.GroupMember;
import org.be.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupAndUser(UserGroup userGroup, User user);

    List<GroupMember> findByGroup(UserGroup userGroup);

    List<GroupMember> findByUser(User user); // 사용자가 참여한 그룹 리스트 조회
}