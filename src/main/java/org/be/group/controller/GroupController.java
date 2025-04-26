package org.be.group.controller;

import lombok.RequiredArgsConstructor;
import org.be.group.dto.GroupCreateRequestDto;
import org.be.group.dto.GroupResponseDto;
import org.be.group.service.GroupMemberService;
import org.be.group.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    // 그룹 생성
    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(@RequestBody GroupCreateRequestDto requestDto,
                                                        Authentication authentication) {
        return ResponseEntity.ok(groupService.createGroup(requestDto, authentication));
    }

    // 전체 그룹 조회
    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }
}