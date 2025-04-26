package org.be.group.controller;

import lombok.RequiredArgsConstructor;
import org.be.group.dto.GroupMemberResponseDto;
import org.be.group.dto.GroupResponseDto;
import org.be.group.service.GroupMemberService;
import org.be.auth.service.CustomUserDetails;
import org.springframework.http.ResponseEntity;
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
        groupMemberService.joinGroup(groupId, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupMemberService.leaveGroup(groupId, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<GroupMemberResponseDto>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberResponseDto> members = groupMemberService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/kick/{userId}")
    public ResponseEntity<Void> kickMember(@PathVariable Long groupId,
                                           @PathVariable Long userId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupMemberService.kickMember(groupId, userDetails.getUser(), userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer/{userId}")
    public ResponseEntity<Void> transferLeadership(@PathVariable Long groupId,
                                                   @PathVariable Long userId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupMemberService.transferLeadership(groupId, userDetails.getUser(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<GroupResponseDto>> getMyGroups(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GroupResponseDto> myGroups = groupMemberService.getMyGroups(userDetails.getUser());
        return ResponseEntity.ok(myGroups);
    }
}