package com.interviewroulette.interviewpracticeservice.controller;

import com.interviewroulette.interviewpracticeservice.dto.practice.PracticeSessionResponse;
import com.interviewroulette.interviewpracticeservice.dto.practice.StartPracticeRequest;
import com.interviewroulette.interviewpracticeservice.service.practice.PracticeSessionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice/sessions")
public class PracticeSessionController {

    private final PracticeSessionService practiceSessionService;

    public PracticeSessionController(
            PracticeSessionService practiceSessionService
    ) {
        this.practiceSessionService = practiceSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PracticeSessionResponse startPractice(
            @Valid @RequestBody StartPracticeRequest request
    ) {
        return practiceSessionService.startPractice(request);
    }

    @PostMapping("/{sessionId}/new-round")
    public PracticeSessionResponse startNewRound(
            @PathVariable UUID sessionId,
            @RequestParam UUID candidateId
    ) {
        return practiceSessionService.startNewRound(
                candidateId,
                sessionId
        );
    }
    
    /*
     * We will add an end-point that returns all practice sessions for a candidate, ordered from newest to oldest.
     * */
	@GetMapping
	public List<PracticeSessionResponse> getPracticeHistory(@RequestParam UUID candidateId) {
		return practiceSessionService.getPracticeHistory(candidateId);
	}
}