package com.interviewroulette.interviewpracticeservice.dto.practice;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartPracticeRequest(

        @NotNull(message = "Candidate ID is required")
        UUID candidateId,

        @NotNull(message = "Topic ID is required")
        Long topicId

) {
}