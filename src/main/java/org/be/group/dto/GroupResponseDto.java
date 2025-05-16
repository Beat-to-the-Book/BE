package org.be.group.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupResponseDto {
    private Long id;
    private String name;

    public static GroupResponseDto fromEntity(org.be.group.entity.UserGroup userGroup) {
        return GroupResponseDto.builder()
                .id(userGroup.getId())
                .name(userGroup.getName())
                .build();
    }
}