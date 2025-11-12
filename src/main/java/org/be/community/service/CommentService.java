package org.be.community.service;

import lombok.RequiredArgsConstructor;
import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.community.dto.CommentDto;
import org.be.community.entity.Comment;
import org.be.community.entity.CommunityPost;
import org.be.community.repository.CommentRepository;
import org.be.community.repository.CommunityPostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    // 최상위 작성
    @Transactional
    public CommentDto.Response createRoot(String userId, Long postId, CommentDto.CreateRequest req) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."));

        Comment c = Comment.newRoot(post, user, req.getContent());
        c = commentRepository.save(c);

        // 최상위는 root = self 로 세팅
        if (c.getRoot() == null) {
            c.setRoot(c);
        }
        return toDto(commentRepository.save(c));
    }

    // 대댓글 작성
    @Transactional
    public CommentDto.Response createReply(String userId, Long postId, CommentDto.ReplyRequest req) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."));
        Comment parent = commentRepository.findById(req.getParentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "부모 댓글이 존재하지 않습니다."));

        if (!parent.getPost().getId().equals(post.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "부모 댓글이 해당 글에 속하지 않습니다.");
        }

        Comment c = Comment.newReply(post, user, parent, req.getContent());
        return toDto(commentRepository.save(c));
    }

    // 수정
    @Transactional
    public CommentDto.Response update(String userId, Long commentId, CommentDto.UpdateRequest req) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."));

        if (!c.getAuthor().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 댓글만 수정할 수 있습니다.");
        }
        if (c.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 댓글은 수정할 수 없습니다.");
        }
        c.setContent(req.getContent());
        c.setEdited(true);
        return toDto(c);
    }

    // 삭제(soft delete)
    @Transactional
    public void delete(String userId, Long commentId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."));
        if (!c.getAuthor().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 댓글만 삭제할 수 있습니다.");
        }
        c.setDeleted(true);
        c.setContent("삭제된 댓글입니다.");
    }

    // 평탄 목록 조회(정렬: 스레드-작성시간)
    @Transactional(readOnly = true)
    public List<CommentDto.Response> listFlat(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."));
        var list = commentRepository.findByPostOrderByRootIdAscCreatedAtAsc(post);
        return list.stream().map(this::toDto).toList();
    }

    // 트리형 조회(옵션)
    @Transactional(readOnly = true)
    public List<CommentDto.Response> listTree(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."));
        var all = commentRepository.findByPostOrderByRootIdAscCreatedAtAsc(post);

        Map<Long, CommentDto.Response> map = new LinkedHashMap<>();
        for (var c : all) {
            map.put(c.getId(), toDto(c));
        }
        // 자식 연결
        for (var c : all) {
            if (c.getParent() != null) {
                var parentDto = map.get(c.getParent().getId());
                if (parentDto.getChildren() == null) {
                    parentDto.setChildren(new ArrayList<>());
                }
                parentDto.getChildren().add(map.get(c.getId()));
            }
        }
        // 최상위만 반환
        return map.values().stream()
                .filter(d -> d.getParentId() == null)
                .collect(Collectors.toList());
    }

    private CommentDto.Response toDto(Comment c) {
        return new CommentDto.Response(
                c.getId(),
                c.getParent() == null ? null : c.getParent().getId(),
                c.getRoot() == null ? c.getId() : c.getRoot().getId(),
                c.getDepth(),
                c.getAuthor().getId(),
                c.getAuthor().getUsername(),
                c.isDeleted() ? "삭제된 댓글입니다." : c.getContent(),
                c.isDeleted(),
                c.isEdited(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                null
        );
    }
}