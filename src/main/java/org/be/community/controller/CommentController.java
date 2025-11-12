package org.be.community.controller;

import lombok.RequiredArgsConstructor;
import org.be.community.dto.CommentDto;
import org.be.community.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto.Response> createRoot(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long postId,
            @RequestBody CommentDto.CreateRequest req
    ) {
        return ResponseEntity.ok(commentService.createRoot(user.getUsername(), postId, req));
    }

    @PostMapping("/reply")
    public ResponseEntity<CommentDto.Response> createReply(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long postId,
            @RequestBody CommentDto.ReplyRequest req
    ) {
        return ResponseEntity.ok(commentService.createReply(user.getUsername(), postId, req));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto.Response> update(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long commentId,
            @RequestBody CommentDto.UpdateRequest req
    ) {
        return ResponseEntity.ok(commentService.update(user.getUsername(), commentId, req));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long commentId
    ) {
        commentService.delete(user.getUsername(), commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/flat")
    public ResponseEntity<List<CommentDto.Response>> listFlat(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.listFlat(postId));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CommentDto.Response>> listTree(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.listTree(postId));
    }
}