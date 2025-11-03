package org.be.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
public class PurchaseResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String status;
    private LocalDateTime purchaseDate;
    private LocalDateTime refundRequestedAt;
    private String refundReason;
//
//    public PurchaseResponse(Long id, Long bookId, String bookTitle, String status,
//                            LocalDateTime purchaseDate, LocalDateTime refundRequestedAt, String refundReason) {
//        this.id = id;
//        this.bookId = bookId;
//        this.bookTitle = bookTitle;
//        this.status = status;
//        this.purchaseDate = purchaseDate;
//        this.refundRequestedAt = refundRequestedAt;
//        this.refundReason = refundReason;
//    }
}