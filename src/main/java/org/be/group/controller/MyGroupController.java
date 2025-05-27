package org.be.group.controller;

import lombok.RequiredArgsConstructor;
import org.be.auth.service.CustomUserDetails;
import org.be.group.dto.GroupResponseDto;
import org.be.group.service.GroupMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/my")
class MyGroupController {

    private final GroupMemberService groupMemberService;

    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> getMyGroups(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return ResponseEntity.ok(groupMemberService.getMyGroups(userDetails.getUser()));
    }
}
