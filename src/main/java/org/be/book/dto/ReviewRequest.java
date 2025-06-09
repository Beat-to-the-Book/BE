package org.be.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long bookId;

    @Min(0) @Max(5)
    private int rating;

    @NotBlank
    private String comment;
}