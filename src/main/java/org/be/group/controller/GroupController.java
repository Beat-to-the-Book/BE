package org.be.group.controller;

import lombok.RequiredArgsConstructor;
import org.be.auth.service.CustomUserDetails;
import org.be.group.dto.GroupCreateRequestDto;
import org.be.group.dto.GroupResponseDto;
import org.be.group.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    private void validateAuth(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
    }

    // 그룹 생성
    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@RequestBody GroupCreateRequestDto requestDto,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        validateAuth(userDetails);
        return ResponseEntity.ok(groupService.createGroup(requestDto, userDetails.getUser()));
    }

    // 전체 그룹 조회
    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> getAllGroups(@AuthenticationPrincipal CustomUserDetails userDetails) {
        validateAuth(userDetails);
        return ResponseEntity.ok(groupService.getAllGroups(userDetails.getUser()));
    }
}