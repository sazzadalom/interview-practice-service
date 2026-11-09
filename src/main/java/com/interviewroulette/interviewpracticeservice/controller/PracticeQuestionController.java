
package com.interviewroulette.interviewpracticeservice.controller;

import com.interviewroulette.interviewpracticeservice.dto.NextQuestionResponse;
import com.interviewroulette.interviewpracticeservice.service.practice.PracticeQuestionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice")
public class PracticeQuestionController {

    private final PracticeQuestionService practiceQuestionService;

    public PracticeQuestionController(
            PracticeQuestionService practiceQuestionService
    ) {
        this.practiceQuestionService = practiceQuestionService;
    }

    @GetMapping("/sessions/{sessionId}/next-question")
    public ResponseEntity<NextQuestionResponse> getNextQuestion(
            @PathVariable UUID sessionId,
            @RequestParam UUID candidateId
    ) {

        return practiceQuestionService
                .getNextQuestion(candidateId, sessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
