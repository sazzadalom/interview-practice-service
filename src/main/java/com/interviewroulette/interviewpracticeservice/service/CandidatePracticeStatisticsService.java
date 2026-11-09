package com.interviewroulette.interviewpracticeservice.service;

import com.interviewroulette.interviewpracticeservice.dto.CandidatePracticeStatisticsResponse;
import com.interviewroulette.interviewpracticeservice.repository.PracticeSessionRepository;
import com.interviewroulette.interviewpracticeservice.repository.QuestionAttemptRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CandidatePracticeStatisticsService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final QuestionAttemptRepository questionAttemptRepository;

    public CandidatePracticeStatisticsService(
            PracticeSessionRepository practiceSessionRepository,
            QuestionAttemptRepository questionAttemptRepository
    ) {
        this.practiceSessionRepository = practiceSessionRepository;
        this.questionAttemptRepository = questionAttemptRepository;
    }

    @Transactional(readOnly = true)
    public CandidatePracticeStatisticsResponse getCandidateStatistics(
            UUID candidateId,
            Long topicId
    ) {

        long totalSessions =
                practiceSessionRepository.countCandidateSessions(
                        candidateId, topicId
                );

        long completedSessions =
                practiceSessionRepository.countCandidateSessionsByStatus(
                        candidateId, topicId, "COMPLETED"
                );

        long pausedSessions =
                practiceSessionRepository.countCandidateSessionsByStatus(
                        candidateId, topicId, "PAUSED"
                );

        long totalAttempts =
                questionAttemptRepository.countCandidateAttempts(
                        candidateId, topicId
                );

        long uniqueQuestions =
                questionAttemptRepository.countCandidateUniqueQuestions(
                        candidateId, topicId
                );

        long completedAttempts =
                questionAttemptRepository.countCandidateAttemptsByStatus(
                        candidateId, topicId, "COMPLETED"
                );

        long skippedAttempts =
                questionAttemptRepository.countCandidateAttemptsByStatus(
                        candidateId, topicId, "SKIPPED"
                );

        Long totalDuration =
                questionAttemptRepository.getCandidateTotalDuration(
                        candidateId, topicId
                );

        return new CandidatePracticeStatisticsResponse(
                candidateId,
                topicId,
                totalSessions,
                completedSessions,
                pausedSessions,
                totalAttempts,
                uniqueQuestions,
                completedAttempts,
                skippedAttempts,
                totalDuration != null ? totalDuration : 0L
        );
    }
}