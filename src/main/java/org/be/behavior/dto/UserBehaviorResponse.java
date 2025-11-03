package org.be.behavior.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserBehaviorResponse {
    private Long id;
    private String userId;
    private String username;
    private Long bookId;
    private Integer stayTime;
    private Integer scrollDepth;
    private LocalDateTime timestamp;
}
