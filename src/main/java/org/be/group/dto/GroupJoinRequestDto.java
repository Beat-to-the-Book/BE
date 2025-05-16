package org.be.group.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupJoinRequestDto {

    @NotNull(message = "그룹 ID는 필수입니다.")
    private Long groupId;
}