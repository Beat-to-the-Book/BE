package org.be.behavior.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserBehaviorRequestMessage {
    private String userId;
    private Long bookId;
    private int stayTime;
    private int scrollDepth;
}
