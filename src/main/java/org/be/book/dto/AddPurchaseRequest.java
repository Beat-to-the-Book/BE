package org.be.book.dto;

import lombok.Data;

@Data
public class AddPurchaseRequest {
    private String userId;
    private Long bookId;
}
