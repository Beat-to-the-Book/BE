package org.be.community.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.be.auth.service.CustomUserDetails;
import org.be.community.dto.CommunityPostRequestDto;
import org.be.community.dto.CommunityPostResponseDto;
import org.be.community.entity.CommunityPost;
import org.be.community.repository.CommunityPostRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository postRepository;

    public List<CommunityPostResponseDto> getAllPosts() {
        return postRepository.findAllWithUser()
                .stream()
                .map(CommunityPostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public CommunityPostResponseDto getPostById(Long id) {
        CommunityPost post = postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return CommunityPostResponseDto.fromEntity(post);
    }

    public CommunityPostResponseDto createPost(CommunityPostRequestDto dto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        CommunityPost post = CommunityPost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .user(userDetails.getUser())
                .build();

        return CommunityPostResponseDto.fromEntity(postRepository.save(post));
    }

    @Transactional
    public CommunityPostResponseDto updatePost(Long id, CommunityPostRequestDto dto, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        System.out.println("🔍 현재 로그인한 userId: " + userDetails.getUsername());

        CommunityPost post = postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        System.out.println("📝 글 작성자의 userId: " + post.getUser().getUserId());

        if (!post.getUser().getUserId().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("게시글 작성자만 수정할 수 있습니다.");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        return CommunityPostResponseDto.fromEntity(postRepository.save(post));
    }

    public void deletePost(Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        System.out.println("🔍 현재 로그인한 userId: " + userDetails.getUsername());

        CommunityPost post = postRepository.findByIdWithUser(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        System.out.println("📝 글 작성자의 userId: " + post.getUser().getUserId());

        if (!post.getUser().getUserId().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("게시글 작성자만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

}
