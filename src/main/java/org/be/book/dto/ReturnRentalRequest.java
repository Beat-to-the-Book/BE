package org.be.book.dto;

import lombok.Data;

@Data
public class ReturnRentalRequest {
    private String userId;
    private Long rentalId;
}