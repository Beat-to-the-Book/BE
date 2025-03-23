package org.be.action.dto;

import lombok.Data;

@Data
public class ActionDto {
    private Long userId;
    private Long bookId;
    private String actionType; // click, search, purchase, cart, like, view_time
    private float actionValue;
}
