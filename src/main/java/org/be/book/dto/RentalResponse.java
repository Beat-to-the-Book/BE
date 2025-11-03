package org.be.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RentalResponse {
    private Long rentalId;
    private Long bookId;
    private String title;
    private LocalDateTime rentalDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
}