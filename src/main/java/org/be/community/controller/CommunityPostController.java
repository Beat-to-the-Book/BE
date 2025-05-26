package org.be.community.controller;

import lombok.RequiredArgsConstructor;
import org.be.auth.service.CustomUserDetails;
import org.be.community.dto.CommunityPostRequestDto;
import org.be.community.dto.CommunityPostResponseDto;
import org.be.community.service.CommunityPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityPostController {

    private final CommunityPostService postService;

    // 전체 게시글 조회 (모든 커뮤니티의)
    @GetMapping
    public List<CommunityPostResponseDto> getAllPosts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return postService.getAllPosts(userDetails.getUser());
    }

    // 그룹 내 게시글 목록 조회
    @GetMapping("/{groupId}/posts")
    public ResponseEntity<List<CommunityPostResponseDto>> getGroupPosts(@PathVariable Long groupId,
                                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.getPostsByGroup(groupId, userDetails.getUser()));
    }

    // 게시글 단건 조회
    @GetMapping("/{groupId}/posts/{postId}")
    public ResponseEntity<CommunityPostResponseDto> getPost(@PathVariable Long groupId,
                                                            @PathVariable Long postId,
                                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.getPost(groupId, postId, userDetails.getUser()));
    }

    // 게시글 작성
    @PostMapping("/{groupId}/posts")
    public ResponseEntity<CommunityPostResponseDto> createPost(@PathVariable Long groupId,
                                                               @RequestBody CommunityPostRequestDto requestDto,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.createPost(groupId, requestDto, userDetails.getUser()));
    }

    // 게시글 수정 (작성자만)
    @PutMapping("/{groupId}/posts/{postId}")
    public ResponseEntity<CommunityPostResponseDto> updatePost(@PathVariable Long groupId,
                                                               @PathVariable Long postId,
                                                               @RequestBody CommunityPostRequestDto requestDto,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.updatePost(groupId, postId, requestDto, userDetails.getUser()));
    }

    // 게시글 삭제 (작성자 또는 방장)
    @DeleteMapping("/{groupId}/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long groupId,
                                           @PathVariable Long postId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePost(groupId, postId, userDetails.getUser());
        return ResponseEntity.ok().build();
    }
}
