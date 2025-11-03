package org.be.book.dto;

import java.time.LocalDateTime;
import org.be.point.dto.MilestoneAwardResponse;

public class RentalResponse {
    private Long rentalId;
    private Long bookId;
    private String title;
    private LocalDateTime rentalDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;

    private MilestoneAwardResponse milestone;

    public RentalResponse(Long rentalId, Long bookId, String title,
                          LocalDateTime rentalDate, LocalDateTime dueDate,
                          LocalDateTime returnDate, String status,
                          MilestoneAwardResponse milestone) {
        this.rentalId = rentalId;
        this.bookId = bookId;
        this.title = title;
        this.rentalDate = rentalDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.milestone = milestone;
    }

    public Long getRentalId() { return rentalId; }
    public Long getBookId() { return bookId; }
    public String getTitle() { return title; }
    public LocalDateTime getRentalDate() { return rentalDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public String getStatus() { return status; }
    public MilestoneAwardResponse getMilestone() { return milestone; }
}