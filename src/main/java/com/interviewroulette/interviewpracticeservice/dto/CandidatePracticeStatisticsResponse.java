package com.interviewroulette.interviewpracticeservice.dto;

import java.util.UUID;

public record CandidatePracticeStatisticsResponse(
        UUID candidateId,
        Long topicId,
        long totalSessions,
        long completedSessions,
        long pausedSessions,
        long totalAttempts,
        long uniqueQuestions,
        long completedAttempts,
        long skippedAttempts,
        long totalDurationSeconds
) {
}