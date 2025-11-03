package org.be.behavior.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserBehaviorRequest {
    private Long bookId;
    private int stayTime; // seconds
    private int scrollDepth; // percentage
    private LocalDateTime timestamp;
}
