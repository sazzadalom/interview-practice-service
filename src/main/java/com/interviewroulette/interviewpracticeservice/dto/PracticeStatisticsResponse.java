package com.interviewroulette.interviewpracticeservice.dto;

import java.util.UUID;

public record PracticeStatisticsResponse(
        UUID sessionId,
        long totalAttempts,
        long uniqueQuestions,
        long completedAttempts,
        long skippedAttempts,
        long totalDurationSeconds,
        double averageDurationSeconds,
        double completionPercentage
) {
}