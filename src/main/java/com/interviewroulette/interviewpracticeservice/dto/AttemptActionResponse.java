package com.interviewroulette.interviewpracticeservice.dto;

import java.time.Instant;
import java.util.UUID;

public record AttemptActionResponse(
        UUID attemptId,
        UUID sessionId,
        UUID questionId,
        String status,
        Instant startedAt,
        Instant completedAt,
        Long durationSeconds
) {
}