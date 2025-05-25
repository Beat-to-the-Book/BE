package org.be.group.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupRoleResponseDto {
    private Long groupId;
    private String userId;
    private boolean isMember;
    private boolean isLeader;
}
