package org.be.point.dto;

public record ThrowBookResponse(Long bookId, boolean thrown, int pointsAwarded, int totalPoints) {}