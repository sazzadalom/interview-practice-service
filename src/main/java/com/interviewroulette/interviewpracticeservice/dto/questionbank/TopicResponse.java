package com.interviewroulette.interviewpracticeservice.dto.questionbank;

import java.time.Instant;


/*
 * This DTOs represent the Question Bank responses from the implementation we created in the question bank service.
 * 
 * **/
public record TopicResponse(
        Long id,
        String name,
        String slug,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}