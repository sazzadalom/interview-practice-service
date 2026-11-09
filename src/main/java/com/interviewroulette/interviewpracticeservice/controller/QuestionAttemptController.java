
package com.interviewroulette.interviewpracticeservice.controller;

import com.interviewroulette.interviewpracticeservice.dto.AttemptActionResponse;
import com.interviewroulette.interviewpracticeservice.service.practice.QuestionAttemptService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice")
public class QuestionAttemptController {

    private final QuestionAttemptService questionAttemptService;

    public QuestionAttemptController(
            QuestionAttemptService questionAttemptService
    ) {
        this.questionAttemptService = questionAttemptService;
    }

    @PostMapping("/sessions/{sessionId}/attempts/{attemptId}/complete")
    public ResponseEntity<AttemptActionResponse> completeQuestion(
            @PathVariable UUID sessionId,
            @PathVariable UUID attemptId,
            @RequestParam UUID candidateId
    ) {

        return ResponseEntity.ok(
                questionAttemptService.completeQuestion(
                        candidateId,
                        sessionId,
                        attemptId
                )
        );
    }

    @PostMapping("/sessions/{sessionId}/attempts/{attemptId}/skip")
    public ResponseEntity<AttemptActionResponse> skipQuestion(
            @PathVariable UUID sessionId,
            @PathVariable UUID attemptId,
            @RequestParam UUID candidateId
    ) {

        return ResponseEntity.ok(
                questionAttemptService.skipQuestion(
                        candidateId,
                        sessionId,
                        attemptId
                )
        );
    }
}
