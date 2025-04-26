package org.be.group.service;

import lombok.RequiredArgsConstructor;
import org.be.group.dto.GroupCreateRequestDto;
import org.be.group.dto.GroupResponseDto;
import org.be.group.entity.UserGroup;
import org.be.group.entity.GroupMember;
import org.be.group.repository.GroupMemberRepository;
import org.be.group.repository.GroupRepository;
import org.be.auth.model.User;
import org.be.auth.service.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupResponseDto createGroup(GroupCreateRequestDto requestDto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (groupRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 그룹 이름입니다.");
        }

        UserGroup group = UserGroup.builder()
                .name(requestDto.getName())
                .build();
        groupRepository.save(group);

        // 그룹 생성자는 자동으로 방장 등록
        GroupMember groupMember = GroupMember.builder()
                .user(user)
                .group(group)
                .role(GroupMember.Role.LEADER)
                .build();
        groupMemberRepository.save(groupMember);

        return GroupResponseDto.fromEntity(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> getAllGroups() {
        return groupRepository.findAll()
                .stream()
                .map(GroupResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> getMyGroups(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        return groupMemberRepository.findByUser(user)
                .stream()
                .map(GroupMember::getGroup)
                .map(GroupResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserGroup findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
    }
}