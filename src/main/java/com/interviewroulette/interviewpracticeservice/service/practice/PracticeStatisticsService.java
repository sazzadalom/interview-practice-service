package com.interviewroulette.interviewpracticeservice.service.practice;

import com.interviewroulette.interviewpracticeservice.dto.PracticeStatisticsResponse;
import com.interviewroulette.interviewpracticeservice.entity.PracticeSession;
import com.interviewroulette.interviewpracticeservice.repository.PracticeSessionRepository;
import com.interviewroulette.interviewpracticeservice.repository.QuestionAttemptRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PracticeStatisticsService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final QuestionAttemptRepository questionAttemptRepository;

    public PracticeStatisticsService(
            PracticeSessionRepository practiceSessionRepository,
            QuestionAttemptRepository questionAttemptRepository
    ) {
        this.practiceSessionRepository = practiceSessionRepository;
        this.questionAttemptRepository = questionAttemptRepository;
    }

    @Transactional(readOnly = true)
    public PracticeStatisticsResponse getSessionStatistics(
            UUID candidateId,
            UUID sessionId
    ) {

        PracticeSession session = practiceSessionRepository
                .findByIdAndCandidateId(sessionId, candidateId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Practice session not found")
                );

        long totalAttempts =
                questionAttemptRepository.countBySessionId(sessionId);

        long uniqueQuestions =
                questionAttemptRepository.countDistinctQuestionsBySessionId(sessionId);

        long completedAttempts =
                questionAttemptRepository.countBySessionIdAndStatus(
                        sessionId, "COMPLETED"
                );

        long skippedAttempts =
                questionAttemptRepository.countBySessionIdAndStatus(
                        sessionId, "SKIPPED"
                );

        Long totalDuration =
                questionAttemptRepository.getTotalDurationBySessionId(sessionId);

        long totalDurationSeconds =
                totalDuration != null ? totalDuration : 0L;

        long finishedAttempts =
                questionAttemptRepository.countFinishedAttemptsBySessionId(sessionId);

        double averageDurationSeconds =
                finishedAttempts == 0
                        ? 0.0
                        : (double) totalDurationSeconds / finishedAttempts;

        double completionPercentage =
                finishedAttempts == 0
                        ? 0.0
                        : (double) completedAttempts / finishedAttempts * 100.0;

        return new PracticeStatisticsResponse(
                session.getId(),
                totalAttempts,
                uniqueQuestions,
                completedAttempts,
                skippedAttempts,
                totalDurationSeconds,
                averageDurationSeconds,
                completionPercentage
        );
    }
}