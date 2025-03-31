package org.be.book.dto;

import org.be.book.model.Purchase;

public class HistoryPurchaseResponse {
    private Long purchaseId;
    private String bookTitle;
    private String bookAuthor;

    public HistoryPurchaseResponse(Purchase purchase) {
        this.purchaseId = purchase.getId();
        this.bookTitle = purchase.getBook().getTitle(); // Book은 EAGER거나 미리 초기화된 상태여야 함
        this.bookAuthor = purchase.getBook().getAuthor();
    }
}

