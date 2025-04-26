package org.be.community.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.be.auth.model.User;
import org.be.community.dto.CommunityPostRequestDto;
import org.be.community.dto.CommunityPostResponseDto;
import org.be.community.entity.CommunityPost;
import org.be.community.repository.CommunityPostRepository;
import org.be.group.entity.UserGroup;
import org.be.group.service.GroupMemberService;
import org.be.group.service.GroupService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    public List<CommunityPostResponseDto> getAllPosts() {
        return postRepository.findAllWithUser()
                .stream()
                .map(CommunityPostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public CommunityPostResponseDto getPost(Long id) {
        CommunityPost post = postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return CommunityPostResponseDto.fromEntity(post);
    }

    public CommunityPostResponseDto createPost(Long groupId, CommunityPostRequestDto dto, User user) {
        UserGroup group = groupService.findGroupById(groupId);

        if (!groupMemberService.isMember(groupId, user)) {
            throw new IllegalStateException("그룹에 가입된 사용자만 글을 작성할 수 있습니다.");
        }

        CommunityPost post = CommunityPost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .user(user)
                .group(group)
                .build();

        return CommunityPostResponseDto.fromEntity(postRepository.save(post));
    }

    @Transactional
    public CommunityPostResponseDto updatePost(Long id, CommunityPostRequestDto dto, User user) {
        CommunityPost post = postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("게시글 작성자만 수정할 수 있습니다.");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        return CommunityPostResponseDto.fromEntity(postRepository.save(post));
    }

    public void deletePost(Long postId, User user) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        boolean isAuthor = post.getUser().getId().equals(user.getId());
        boolean isLeader = groupMemberService.isLeader(post.getGroup().getId(), user);

        if (!isAuthor && !isLeader) {
            throw new IllegalStateException("게시글 작성자나 그룹의 방장만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }
}
