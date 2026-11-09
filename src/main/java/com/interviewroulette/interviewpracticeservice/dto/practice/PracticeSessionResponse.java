package com.interviewroulette.interviewpracticeservice.dto.practice;

import java.time.Instant;
import java.util.UUID;

public record PracticeSessionResponse(

        UUID sessionId,

        UUID candidateId,

        Long topicId,

        String topicName,

        String status,

        Integer roundNumber,

        Instant startedAt,

        Instant completedAt

) {
}