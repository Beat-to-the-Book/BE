package org.be.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupCreateRequestDto {
    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String name;
}