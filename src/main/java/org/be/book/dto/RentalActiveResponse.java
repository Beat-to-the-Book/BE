package org.be.book.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RentalActiveResponse {
    private Long rentalId;
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private LocalDate startDate;
    private LocalDate dueDate;
    private long daysRemaining;
}