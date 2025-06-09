package org.be.book.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private Long bookId;
    private String content;
    private int rating;
    private Boolean publicVisible;
}