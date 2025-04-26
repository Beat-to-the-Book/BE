package org.be.group.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.be.group.dto.GroupMemberResponseDto;
import org.be.group.dto.GroupResponseDto;
import org.be.group.entity.UserGroup;
import org.be.group.entity.GroupMember;
import org.be.group.repository.GroupMemberRepository;
import org.be.auth.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupService groupService;

    public void joinGroup(Long groupId, User user) {
        UserGroup userGroup = groupService.findGroupById(groupId);

        if (groupMemberRepository.findByGroupAndUser(userGroup, user).isPresent()) {
            throw new IllegalStateException("이미 그룹에 가입되어 있습니다.");
        }

        GroupMember member = GroupMember.builder()
                .user(user)
                .group(userGroup)
                .role(GroupMember.Role.MEMBER)
                .build();
        groupMemberRepository.save(member);
    }

    public void leaveGroup(Long groupId, User user) {
        UserGroup userGroup = groupService.findGroupById(groupId);
        GroupMember member = groupMemberRepository.findByGroupAndUser(userGroup, user)
                .orElseThrow(() -> new IllegalStateException("그룹에 가입되어 있지 않습니다."));

        if (member.getRole() == GroupMember.Role.LEADER) {
            throw new IllegalStateException("방장은 권한을 위임한 후 탈퇴할 수 있습니다.");
        }

        groupMemberRepository.delete(member);
    }

    public List<GroupMemberResponseDto> getGroupMembers(Long groupId) {
        UserGroup userGroup = groupService.findGroupById(groupId);

        return groupMemberRepository.findByGroup(userGroup).stream()
                .map(GroupMemberResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public void kickMember(Long groupId, User leaderUser, Long targetUserId) {
        UserGroup userGroup = groupService.findGroupById(groupId);

        GroupMember leader = groupMemberRepository.findByGroupAndUser(userGroup, leaderUser)
                .orElseThrow(() -> new IllegalStateException("그룹 소속이 아닙니다."));

        if (leader.getRole() != GroupMember.Role.LEADER) {
            throw new IllegalStateException("방장만 다른 멤버를 강퇴할 수 있습니다.");
        }

        GroupMember target = groupMemberRepository.findByGroup(userGroup).stream()
                .filter(m -> m.getUser().getId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자가 그룹에 존재하지 않습니다."));

        if (target.getRole() == GroupMember.Role.LEADER) {
            throw new IllegalStateException("방장은 스스로를 강퇴할 수 없습니다.");
        }

        groupMemberRepository.delete(target);
    }

    public void transferLeadership(Long groupId, User currentLeaderUser, Long newLeaderUserId) {
        UserGroup userGroup = groupService.findGroupById(groupId);

        GroupMember currentLeader = groupMemberRepository.findByGroupAndUser(userGroup, currentLeaderUser)
                .orElseThrow(() -> new IllegalStateException("그룹 소속이 아닙니다."));

        if (currentLeader.getRole() != GroupMember.Role.LEADER) {
            throw new IllegalStateException("현재 사용자는 방장이 아닙니다.");
        }

        GroupMember newLeader = groupMemberRepository.findByGroup(userGroup).stream()
                .filter(m -> m.getUser().getId().equals(newLeaderUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("새 방장 대상 사용자가 그룹에 존재하지 않습니다."));

        // 권한 교체
        currentLeader.setRole(GroupMember.Role.MEMBER);
        newLeader.setRole(GroupMember.Role.LEADER);

        groupMemberRepository.save(currentLeader);
        groupMemberRepository.save(newLeader);
    }

    public List<GroupResponseDto> getMyGroups(User user) {
        return groupMemberRepository.findByUser(user)
                .stream()
                .map(groupMember -> GroupResponseDto.fromEntity(groupMember.getGroup()))
                .collect(Collectors.toList());
    }

    public boolean isMember(Long groupId, User user) {
        UserGroup group = groupService.findGroupById(groupId);
        return groupMemberRepository.findByGroupAndUser(group, user).isPresent();
    }

    public boolean isLeader(Long groupId, User user) {
        UserGroup group = groupService.findGroupById(groupId);
        return groupMemberRepository.findByGroupAndUser(group, user)
                .filter(m -> m.getRole() == GroupMember.Role.LEADER)
                .isPresent();
    }
}