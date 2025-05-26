package org.be.behavior.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBehaviorRequest {
    private Long bookId;
    private int stayTime; // seconds
    private int scrollDepth; // percentage
}
