package org.be.community.dto;

import lombok.Builder;
import lombok.Getter;
import org.be.community.entity.CommunityPost;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommunityPostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommunityPostResponseDto fromEntity(CommunityPost post) {
        return CommunityPostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .userName(post.getUser().getUsername())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
