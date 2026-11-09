package com.interviewroulette.interviewpracticeservice.dto.questionbank;

import java.time.Instant;
import java.util.UUID;

/*
 * This DTOs represent the Question Bank responses from the implementation we created in the question bank service.
 * 
 * **/
public record QuestionResponse(
        UUID id,
        Long topicId,
        String topicName,
        String questionText,
        String difficulty,
        String expectedAnswer,
        Instant createdAt
) {
}