package com.interviewroulette.interviewpracticeservice.dto;

import java.time.Instant;
import java.util.UUID;

/*
 * We deliberately don't return the expected answer as part of the candidate's question presentation.
 * It should not be exposed before the candidate completes or skips the question.
 * */
public record NextQuestionResponse(
        UUID attemptId,
        UUID sessionId,
        UUID questionId,
        Integer attemptSequence,
        String questionText,
        String difficulty,
//        String expectedAnswer,
        Instant startedAt
) {
}