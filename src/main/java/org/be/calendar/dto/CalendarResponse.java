package org.be.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CalendarResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String author;
    private String frontCoverImageUrl;
    private String startDate;
    private String endDate;
    private String memo;
}