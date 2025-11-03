package org.be.book.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RefundPurchaseRequest {
    private String userId;
    private Long purchaseId;
    private String reason;
}