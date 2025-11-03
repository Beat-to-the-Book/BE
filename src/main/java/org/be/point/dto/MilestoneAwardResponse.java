package org.be.point.dto;

public record MilestoneAwardResponse(
        int totalBooks,
        int milestone,
        int awarded,
        int totalPoints
) {}