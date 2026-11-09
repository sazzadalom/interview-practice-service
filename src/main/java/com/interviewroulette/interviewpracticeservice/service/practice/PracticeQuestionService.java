
package com.interviewroulette.interviewpracticeservice.service.practice;

import com.interviewroulette.interviewpracticeservice.client.QuestionBankClient;
import com.interviewroulette.interviewpracticeservice.dto.NextQuestionResponse;
import com.interviewroulette.interviewpracticeservice.dto.questionbank.QuestionResponse;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PracticeQuestionService {

    private static final String ACTIVE = "ACTIVE";
    private static final String COMPLETED = "COMPLETED";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String SKIPPED = "SKIPPED";

    private final QuestionBankClient questionBankClient;
    private final PracticeSessionRepository practiceSessionRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final UserPracticeContextRepository userPracticeContextRepository;

    public PracticeQuestionService(
            QuestionBankClient questionBankClient,
            PracticeSessionRepository practiceSessionRepository,
            QuestionAttemptRepository questionAttemptRepository,
            UserPracticeContextRepository userPracticeContextRepository
    ) {
        this.questionBankClient = questionBankClient;
        this.practiceSessionRepository = practiceSessionRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.userPracticeContextRepository = userPracticeContextRepository;
    }

    @Transactional
    public Optional<NextQuestionResponse> getNextQuestion(
            UUID candidateId,
            UUID sessionId
    ) {

        // 1. Validate the session.
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

        // 2. Validate the candidate's active practice context.
        UserPracticeContext context = userPracticeContextRepository
                .findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Active practice context not found"
                ));

        if (!sessionId.equals(context.getActiveSessionId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This is not the candidate's active practice session"
            );
        }

        // 3. Retrieve all attempts from this round.
        List<QuestionAttempt> attempts =
                questionAttemptRepository
                        .findBySessionIdOrderByAttemptSequenceAsc(sessionId);

        // 4. If a question is already in progress, return it.
        Optional<QuestionAttempt> currentAttempt =
                attempts.stream()
                        .filter(a -> IN_PROGRESS.equals(a.getStatus()))
                        .findFirst();

        if (currentAttempt.isPresent()) {
            return Optional.of(buildResponse(currentAttempt.get()));
        }

        // 5. Retrieve candidate-uploaded questions.
        List<QuestionResponse> uploadedQuestions =
                questionBankClient.getCandidateQuestionsByTopic(
                        candidateId,
                        session.getTopicId()
                );

        // 6. Retrieve all questions from the shared topic bank.
        List<QuestionResponse> sharedQuestions =
                questionBankClient.getQuestionsByTopic(
                        session.getTopicId()
                );

        // 7. Collect question history.
        Set<UUID> presentedQuestionIds = new HashSet<>();
        Set<UUID> completedQuestionIds = new HashSet<>();
        Set<UUID> uploadedQuestionIds = new HashSet<>();

        for (QuestionAttempt attempt : attempts) {
            presentedQuestionIds.add(attempt.getQuestionId());

            if (COMPLETED.equals(attempt.getStatus())) {
                completedQuestionIds.add(attempt.getQuestionId());
            }
        }

        for (QuestionResponse question : uploadedQuestions) {
            uploadedQuestionIds.add(question.id());
        }

        // 8. Find unseen candidate-uploaded questions.
        List<QuestionResponse> unseenUploaded =
                uploadedQuestions.stream()
                        .filter(q -> !presentedQuestionIds.contains(q.id()))
                        .filter(q -> !completedQuestionIds.contains(q.id()))
                        .toList();

        if (!unseenUploaded.isEmpty()) {
            return Optional.of(
                    createAttemptAndBuildResponse(
                            sessionId,
                            attempts,
                            selectRandom(unseenUploaded)
                    )
            );
        }

        // 9. Find unseen shared questions.
        // Exclude candidate-uploaded questions from this pool.
        List<QuestionResponse> unseenShared =
                sharedQuestions.stream()
                        .filter(q -> !uploadedQuestionIds.contains(q.id()))
                        .filter(q -> !presentedQuestionIds.contains(q.id()))
                        .filter(q -> !completedQuestionIds.contains(q.id()))
                        .toList();

        if (!unseenShared.isEmpty()) {
            return Optional.of(
                    createAttemptAndBuildResponse(
                            sessionId,
                            attempts,
                            selectRandom(unseenShared)
                    )
            );
        }

        // 10. All unseen questions have been presented.
        // Now skipped questions become eligible again.
        Set<UUID> skippedQuestionIds = new LinkedHashSet<>();

        for (QuestionAttempt attempt : attempts) {
            if (SKIPPED.equals(attempt.getStatus())
                    && !completedQuestionIds.contains(attempt.getQuestionId())) {

                skippedQuestionIds.add(attempt.getQuestionId());
            }
        }

        if (!skippedQuestionIds.isEmpty()) {

            List<UUID> skippedList =
                    new ArrayList<>(skippedQuestionIds);

            UUID selectedQuestionId = skippedList.get(
                    ThreadLocalRandom.current().nextInt(skippedList.size())
            );

            QuestionResponse selectedQuestion =
                    questionBankClient.getQuestion(selectedQuestionId);

            return Optional.of(
                    createAttemptAndBuildResponse(
                            sessionId,
                            attempts,
                            selectedQuestion
                    )
            );
        }

        // 11. No eligible questions remain. Complete the round.
        session.setStatus(COMPLETED);
        session.setCompletedAt(Instant.now());

        practiceSessionRepository.save(session);

        context.setActiveSessionId(null);
        userPracticeContextRepository.save(context);

        return Optional.empty();
    }

    private QuestionResponse selectRandom(
            List<QuestionResponse> questions
    ) {
        int index = ThreadLocalRandom.current()
                .nextInt(questions.size());

        return questions.get(index);
    }

    private NextQuestionResponse createAttemptAndBuildResponse(
            UUID sessionId,
            List<QuestionAttempt> attempts,
            QuestionResponse question
    ) {

        int nextSequence = attempts.stream()
                .map(QuestionAttempt::getAttemptSequence)
                .filter(sequence -> sequence != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        QuestionAttempt attempt = new QuestionAttempt();

        attempt.setId(UUID.randomUUID());
        attempt.setSessionId(sessionId);
        attempt.setQuestionId(question.id());
        attempt.setStatus(IN_PROGRESS);
        attempt.setStartedAt(Instant.now());
        attempt.setAttemptSequence(nextSequence);

        questionAttemptRepository.save(attempt);

        return new NextQuestionResponse(
                attempt.getId(),
                sessionId,
                question.id(),
                nextSequence,
                question.questionText(),
                question.difficulty(),
//                question.expectedAnswer(),
                attempt.getStartedAt()
        );
    }

    private NextQuestionResponse buildResponse(
            QuestionAttempt attempt
    ) {

        QuestionResponse question =
                questionBankClient.getQuestion(attempt.getQuestionId());

        return new NextQuestionResponse(
                attempt.getId(),
                attempt.getSessionId(),
                attempt.getQuestionId(),
                attempt.getAttemptSequence(),
                question.questionText(),
                question.difficulty(),
//                question.expectedAnswer(),
                attempt.getStartedAt()
        );
    }
}
