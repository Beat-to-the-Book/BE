package org.be.community.controller;

import lombok.RequiredArgsConstructor;
import org.be.community.dto.CommunityPostRequestDto;
import org.be.community.dto.CommunityPostResponseDto;
import org.be.community.service.CommunityPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community/posts")
public class CommunityPostController {

    private final CommunityPostService postService;

    @GetMapping
    public List<CommunityPostResponseDto> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public CommunityPostResponseDto getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    @PostMapping
    public CommunityPostResponseDto createPost(@RequestBody CommunityPostRequestDto dto,
                                               Authentication authentication) {
        return postService.createPost(dto, authentication);
    }

    @PutMapping("/{id}")
    public CommunityPostResponseDto updatePost(@PathVariable Long id,
                                               @RequestBody CommunityPostRequestDto dto,
                                               Authentication authentication) {
        return postService.updatePost(id, dto, authentication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        postService.deletePost(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
