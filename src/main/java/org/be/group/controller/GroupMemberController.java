package org.be.group.controller;

import lombok.RequiredArgsConstructor;
import org.be.group.dto.GroupMemberResponseDto;
import org.be.group.dto.GroupRoleResponseDto;
import org.be.group.service.GroupMemberService;
import org.be.auth.service.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/members")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @PostMapping("/join")
    public ResponseEntity<Void> joinGroup(@PathVariable Long groupId,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateUser(userDetails);
        groupMemberService.joinGroup(groupId, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateUser(userDetails);
        groupMemberService.leaveGroup(groupId, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembers(@PathVariable Long groupId,
                                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateUser(userDetails);

        if (!groupMemberService.isMember(groupId, userDetails.getUser())) {
            throw new AccessDeniedException("해당 그룹에 가입한 사용자만 멤버 목록을 조회할 수 있습니다.");
        }

        List<GroupMemberResponseDto> members = groupMemberService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/kick/{userId}")
    public ResponseEntity<Void> kickMember(@PathVariable Long groupId,
                                           @PathVariable Long userId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateUser(userDetails);
        groupMemberService.kickMember(groupId, userDetails.getUser(), userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer/{userId}")
    public ResponseEntity<Void> transferLeadership(@PathVariable Long groupId,
                                                   @PathVariable Long userId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateUser(userDetails);
        groupMemberService.transferLeadership(groupId, userDetails.getUser(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/role")
    public ResponseEntity<GroupRoleResponseDto> getMyGroupRole(@PathVariable Long groupId,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateUser(userDetails);

        boolean isMember = groupMemberService.isMember(groupId, userDetails.getUser());
        boolean isLeader = groupMemberService.isLeader(groupId, userDetails.getUser());

        GroupRoleResponseDto response = GroupRoleResponseDto.builder()
                .groupId(groupId)
                .userId(userDetails.getUser().getUserId())
                .isMember(isMember)
                .isLeader(isLeader)
                .build();

        return ResponseEntity.ok(response);
    }

    private void validateUser(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
    }
}