package com.interviewroulette.interviewpracticeservice.repository;

import com.interviewroulette.interviewpracticeservice.entity.PracticeSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PracticeSessionRepository extends JpaRepository<PracticeSession, UUID> {

	List<PracticeSession> findByCandidateIdOrderByStartedAtDesc(UUID candidateId);

	Optional<PracticeSession> findByIdAndCandidateId(UUID id, UUID candidateId);

	/*
	 * This allows us to find the candidate's most recent session for a particular
	 * topic and calculate the next round number.
	 */
	Optional<PracticeSession> findFirstByCandidateIdAndTopicIdOrderByStartedAtDesc(UUID candidateId, Long topicId);
	
	@Query("""
		    SELECT COUNT(ps)
		    FROM PracticeSession ps
		    WHERE ps.candidateId = :candidateId
		      AND (:topicId IS NULL OR ps.topicId = :topicId)
		    """)
		long countCandidateSessions(
		        @Param("candidateId") UUID candidateId,
		        @Param("topicId") Long topicId
		);

		@Query("""
		    SELECT COUNT(ps)
		    FROM PracticeSession ps
		    WHERE ps.candidateId = :candidateId
		      AND ps.status = :status
		      AND (:topicId IS NULL OR ps.topicId = :topicId)
		    """)
		long countCandidateSessionsByStatus(
		        @Param("candidateId") UUID candidateId,
		        @Param("topicId") Long topicId,
		        @Param("status") String status
		);
		
		Optional<PracticeSession> findFirstByCandidateIdAndTopicIdAndStatusOrderByStartedAtDesc(
		        UUID candidateId,
		        Long topicId,
		        String status
		);
}