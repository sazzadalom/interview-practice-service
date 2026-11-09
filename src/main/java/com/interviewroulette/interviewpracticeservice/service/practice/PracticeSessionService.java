package com.interviewroulette.interviewpracticeservice.service.practice;

import com.interviewroulette.interviewpracticeservice.client.QuestionBankClient;
import com.interviewroulette.interviewpracticeservice.dto.practice.PracticeSessionResponse;
import com.interviewroulette.interviewpracticeservice.dto.practice.StartPracticeRequest;
import com.interviewroulette.interviewpracticeservice.dto.questionbank.TopicResponse;
import com.interviewroulette.interviewpracticeservice.entity.PracticeSession;
import com.interviewroulette.interviewpracticeservice.entity.UserPracticeContext;
import com.interviewroulette.interviewpracticeservice.repository.PracticeSessionRepository;
import com.interviewroulette.interviewpracticeservice.repository.UserPracticeContextRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
/*
 * 
 * 
 * <p> This preserves the previous session records instead of deleting them. </p>
 * <h3>Session status handling:</h3>
 * Situation						Session status
 * New session						ACTIVE
 * Candidate switches topic 
 * Previous session becomes 		PAUSED
 * 
 * 
 * Candidate starts a new round
 * Previous round becomes 			COMPLETED
 * 
 * New round starts					ACTIVE
 * 
 * 
 * 
 *
 * 
 * 
 * */
@Service
public class PracticeSessionService {

	private static final String ACTIVE = "ACTIVE";
	private static final String PAUSED = "PAUSED";
	private static final String COMPLETED = "COMPLETED";

	private final PracticeSessionRepository sessionRepository;
	private final UserPracticeContextRepository contextRepository;
	private final QuestionBankClient questionBankClient;

	public PracticeSessionService(PracticeSessionRepository sessionRepository,
			UserPracticeContextRepository contextRepository, QuestionBankClient questionBankClient) {
		this.sessionRepository = sessionRepository;
		this.contextRepository = contextRepository;
		this.questionBankClient = questionBankClient;
	}
/*
	@Transactional
	public PracticeSessionResponse startPractice(StartPracticeRequest request) {

		UUID candidateId = request.candidateId();
		Long topicId = request.topicId();

		// Validate topic through Question Bank Service.
		TopicResponse topic = getActiveTopic(topicId);

		// Find or create candidate practice context.
		UserPracticeContext context = contextRepository.findById(candidateId).orElseGet(() -> {
			UserPracticeContext newContext = new UserPracticeContext();

			newContext.setCandidateId(candidateId);

			return newContext;
		});

		// If the candidate is already practising this topic,
		// return the existing active session.
		if (topicId.equals(context.getActiveTopicId()) && context.getActiveSessionId() != null) {

			PracticeSession existingSession = sessionRepository
					.findByIdAndCandidateId(context.getActiveSessionId(), candidateId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
							"Active practice session not found"));

			if (ACTIVE.equals(existingSession.getStatus())) {
				return mapResponse(existingSession, topic);
			}
		}

		// Pause the previous session when switching topics.
		if (context.getActiveSessionId() != null) {

			sessionRepository.findByIdAndCandidateId(context.getActiveSessionId(), candidateId)
					.ifPresent(previousSession -> {

						if (ACTIVE.equals(previousSession.getStatus())) {
							previousSession.setStatus(PAUSED);
							sessionRepository.save(previousSession);
						}
					});
		}

		// Calculate the next round number for this topic.
		int nextRound = sessionRepository.findFirstByCandidateIdAndTopicIdOrderByStartedAtDesc(candidateId, topicId)
				.map(session -> session.getRoundNumber() + 1).orElse(1);
		

		// Create a new practice session.
		PracticeSession session = new PracticeSession();

		session.setCandidateId(candidateId);
		session.setTopicId(topicId);
		session.setStatus(ACTIVE);
		session.setRoundNumber(nextRound);

		session = sessionRepository.save(session);

		// Update candidate's active practice context.
		context.setActiveTopicId(topicId);
		context.setActiveSessionId(session.getId());

		contextRepository.save(context);

		return mapResponse(session, topic);
	}
*/
	@Transactional
	public PracticeSessionResponse startPractice(StartPracticeRequest request) {

	    UUID candidateId = request.candidateId();
	    Long topicId = request.topicId();

	    // Validate topic through Question Bank Service.
	    TopicResponse topic = getActiveTopic(topicId);

	    // Find or create candidate practice context.
	    UserPracticeContext context = contextRepository.findById(candidateId).orElseGet(() -> {
	        UserPracticeContext newContext = new UserPracticeContext();
	        newContext.setCandidateId(candidateId);
	        return newContext;
	    });

	    // If the candidate is already practising this topic,
	    // return the existing active session.
	    if (topicId.equals(context.getActiveTopicId()) && context.getActiveSessionId() != null) {

	        PracticeSession existingSession = sessionRepository
	                .findByIdAndCandidateId(context.getActiveSessionId(), candidateId)
	                .orElseThrow(() -> new ResponseStatusException(
	                        HttpStatus.NOT_FOUND,
	                        "Active practice session not found"
	                ));

	        if (ACTIVE.equals(existingSession.getStatus())) {
	            return mapResponse(existingSession, topic);
	        }
	    }

	    // Look for the most recently paused session for the requested topic.
	    PracticeSession pausedSession = sessionRepository
	            .findFirstByCandidateIdAndTopicIdAndStatusOrderByStartedAtDesc(
	                    candidateId,
	                    topicId,
	                    PAUSED
	            )
	            .orElse(null);

	    // Pause the candidate's currently active session, if one exists.
	    if (context.getActiveSessionId() != null) {

	        sessionRepository.findByIdAndCandidateId(context.getActiveSessionId(), candidateId)
	                .ifPresent(previousSession -> {

	                    if (ACTIVE.equals(previousSession.getStatus())) {
	                        previousSession.setStatus(PAUSED);
	                        sessionRepository.save(previousSession);
	                    }
	                });
	    }

	    // If a paused session exists for the requested topic,
	    // resume it instead of creating a new round.
	    if (pausedSession != null) {

	        pausedSession.setStatus(ACTIVE);

	        pausedSession = sessionRepository.save(pausedSession);

	        context.setActiveTopicId(topicId);
	        context.setActiveSessionId(pausedSession.getId());

	        contextRepository.save(context);

	        return mapResponse(pausedSession, topic);
	    }

	    // No paused session exists, so calculate the next round number.
	    int nextRound = sessionRepository
	            .findFirstByCandidateIdAndTopicIdOrderByStartedAtDesc(candidateId, topicId)
	            .map(session -> session.getRoundNumber() + 1)
	            .orElse(1);

	    // Create a new practice session.
	    PracticeSession session = new PracticeSession();

	    session.setCandidateId(candidateId);
	    session.setTopicId(topicId);
	    session.setStatus(ACTIVE);
	    session.setRoundNumber(nextRound);

	    session = sessionRepository.save(session);

	    // Update candidate's active practice context.
	    context.setActiveTopicId(topicId);
	    context.setActiveSessionId(session.getId());

	    contextRepository.save(context);

	    return mapResponse(session, topic);
	}  
	@Transactional
	public PracticeSessionResponse startNewRound(UUID candidateId, UUID sessionId) {

		PracticeSession currentSession = sessionRepository.findByIdAndCandidateId(sessionId, candidateId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Practice session not found"));

		UserPracticeContext context = contextRepository.findById(candidateId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Practice context not found"));

		// Only the candidate's currently active session
		// can be used to start a new round.
		if (!sessionId.equals(context.getActiveSessionId()) || !ACTIVE.equals(currentSession.getStatus())) {

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"This is not the candidate's active practice session");
		}

		TopicResponse topic = getActiveTopic(currentSession.getTopicId());

		// Complete the previous round.
		currentSession.setStatus(COMPLETED);
		currentSession.setCompletedAt(java.time.Instant.now());

		sessionRepository.save(currentSession);

		// Create the next round for the same topic.
		PracticeSession newSession = new PracticeSession();

		newSession.setCandidateId(candidateId);
		newSession.setTopicId(currentSession.getTopicId());
		newSession.setRoundNumber(currentSession.getRoundNumber() + 1);
		newSession.setStatus(ACTIVE);

		newSession = sessionRepository.save(newSession);

		// Update active context.
		context.setActiveTopicId(newSession.getTopicId());
		context.setActiveSessionId(newSession.getId());

		contextRepository.save(context);

		return mapResponse(newSession, topic);
	}

	private TopicResponse getActiveTopic(Long topicId) {

		TopicResponse topic = questionBankClient.getTopic(topicId);

		if (topic == null || !topic.active()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Active topic not found");
		}

		return topic;
	}

	private PracticeSessionResponse mapResponse(PracticeSession session, TopicResponse topic) {

		return new PracticeSessionResponse(session.getId(), session.getCandidateId(), session.getTopicId(),
				topic.name(), session.getStatus(), session.getRoundNumber(), session.getStartedAt(),
				session.getCompletedAt());
	}
	
	@Transactional(readOnly = true)
	public List<PracticeSessionResponse> getPracticeHistory(UUID candidateId) {

		List<PracticeSession> sessions = sessionRepository.findByCandidateIdOrderByStartedAtDesc(candidateId);

		return sessions.stream().map(session -> {
			TopicResponse topic = questionBankClient.getTopic(session.getTopicId());

			return new PracticeSessionResponse(session.getId(), session.getCandidateId(), session.getTopicId(),
					topic.name(), session.getStatus(), session.getRoundNumber(), session.getStartedAt(),
					session.getCompletedAt());
		}).toList();
	}
}