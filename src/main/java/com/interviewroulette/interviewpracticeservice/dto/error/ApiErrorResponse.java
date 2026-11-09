package com.interviewroulette.interviewpracticeservice.dto.error;

import java.time.Instant;

// This record defines the common structure of API error responses.
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}