package org.be.point.dto;

public record UserBookItemResponse(
        Long bookId,
        String title,
        String leftCoverImageUrl,
        String frontCoverImageUrl,
        String backCoverImageUrl,
        boolean purchased,
        boolean rented,
        boolean thrown
) {}