package org.be.book.dto;

import lombok.Data;

@Data
public class AddRentalRequest {
    private String userId;
    private Long bookId;
}
