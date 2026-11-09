package com.interviewroulette.interviewpracticeservice.controller;

import com.interviewroulette.interviewpracticeservice.dto.CandidatePracticeStatisticsResponse;
import com.interviewroulette.interviewpracticeservice.service.CandidatePracticeStatisticsService;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice/statistics")
public class CandidatePracticeStatisticsController {

    private final CandidatePracticeStatisticsService statisticsService;

    public CandidatePracticeStatisticsController(
            CandidatePracticeStatisticsService statisticsService
    ) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/candidate")
    public CandidatePracticeStatisticsResponse getCandidateStatistics(
            @RequestParam UUID candidateId,
            @RequestParam(required = false) Long topicId
    ) {
        return statisticsService.getCandidateStatistics(
                candidateId,
                topicId
        );
    }
}