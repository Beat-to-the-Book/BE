package org.be.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommunityPostRequestDto {
    private String title;
    private String content;
}
