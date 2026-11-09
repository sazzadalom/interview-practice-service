package com.interviewroulette.interviewpracticeservice.controller;

import com.interviewroulette.interviewpracticeservice.dto.PracticeStatisticsResponse;
import com.interviewroulette.interviewpracticeservice.service.practice.PracticeStatisticsService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice/statistics")
public class PracticeStatisticsController {

    private final PracticeStatisticsService practiceStatisticsService;

    public PracticeStatisticsController(
            PracticeStatisticsService practiceStatisticsService
    ) {
        this.practiceStatisticsService = practiceStatisticsService;
    }

    @GetMapping("/{sessionId}")
    public PracticeStatisticsResponse getSessionStatistics(
            @PathVariable UUID sessionId,
            @RequestParam UUID candidateId
    ) {
        return practiceStatisticsService.getSessionStatistics(
                candidateId,
                sessionId
        );
    }
}