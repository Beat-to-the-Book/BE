package org.be.calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CalendarCreateRequest {
    private Long bookId;
    private String startDate;
    private String endDate;
    private String memo;
}