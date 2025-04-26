package org.be.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.be.group.entity.GroupMember;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponseDto {
    private Long userId;
    private String username;
    private String role;  // LEADER or MEMBER

    public static GroupMemberResponseDto fromEntity(GroupMember member) {
        return GroupMemberResponseDto.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .role(member.getRole().name())
                .build();
    }
}