
package com.interviewroulette.interviewpracticeservice.service.practice;

import com.interviewroulette.interviewpracticeservice.dto.AttemptActionResponse;
import com.interviewroulette.interviewpracticeservice.entity.PracticeSession;
import com.interviewroulette.interviewpracticeservice.entity.QuestionAttempt;
import com.interviewroulette.interviewpracticeservice.entity.UserPracticeContext;
import com.interviewroulette.interviewpracticeservice.repository.PracticeSessionRepository;
import com.interviewroulette.interviewpracticeservice.repository.QuestionAttemptRepository;
import com.interviewroulette.interviewpracticeservice.repository.UserPracticeContextRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class QuestionAttemptService {

    private static final String ACTIVE = "ACTIVE";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final String SKIPPED = "SKIPPED";

    private final PracticeSessionRepository practiceSessionRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final UserPracticeContextRepository userPracticeContextRepository;

    public QuestionAttemptService(
            PracticeSessionRepository practiceSessionRepository,
            QuestionAttemptRepository questionAttemptRepository,
            UserPracticeContextRepository userPracticeContextRepository
    ) {
        this.practiceSessionRepository = practiceSessionRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.userPracticeContextRepository = userPracticeContextRepository;
    }

    @Transactional
    public AttemptActionResponse completeQuestion(
            UUID candidateId,
            UUID sessionId,
            UUID attemptId
    ) {
        return updateAttempt(
                candidateId,
                sessionId,
                attemptId,
                COMPLETED
        );
    }

    @Transactional
    public AttemptActionResponse skipQuestion(
            UUID candidateId,
            UUID sessionId,
            UUID attemptId
    ) {
        return updateAttempt(
                candidateId,
                sessionId,
                attemptId,
                SKIPPED
        );
    }

    private AttemptActionResponse updateAttempt(
            UUID candidateId,
            UUID sessionId,
            UUID attemptId,
            String newStatus
    ) {

        // 1. Validate candidate's practice session.
        PracticeSession session = practiceSessionRepository
                .findByIdAndCandidateId(sessionId, candidateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Practice session not found"
                ));

        if (!ACTIVE.equals(session.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Practice session is not active"
            );
        }

        // 2. Validate active practice context.
        UserPracticeContext context = userPracticeContextRepository
                .findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Active practice context not found"
                ));

        if (!sessionId.equals(context.getActiveSessionId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This is not the candidate's active session"
            );
        }

        // 3. Find the attempt belonging to this session.
        QuestionAttempt attempt = questionAttemptRepository
                .findByIdAndSessionId(attemptId, sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Question attempt not found"
                ));

        // 4. Only an in-progress attempt can be completed or skipped.
        if (!IN_PROGRESS.equals(attempt.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Question attempt has already been processed"
            );
        }

        // 5. Calculate duration using server timestamps.
        Instant completedAt = Instant.now();

        long durationSeconds = Math.max(
                0,
                Duration.between(
                        attempt.getStartedAt(),
                        completedAt
                ).getSeconds()
        );

        // 6. Update attempt.
        attempt.setStatus(newStatus);
        attempt.setCompletedAt(completedAt);
        attempt.setDurationSeconds(durationSeconds);

        QuestionAttempt savedAttempt =
                questionAttemptRepository.save(attempt);

        return new AttemptActionResponse(
                savedAttempt.getId(),
                savedAttempt.getSessionId(),
                savedAttempt.getQuestionId(),
                savedAttempt.getStatus(),
                savedAttempt.getStartedAt(),
                savedAttempt.getCompletedAt(),
                savedAttempt.getDurationSeconds()
        );
    }
}
