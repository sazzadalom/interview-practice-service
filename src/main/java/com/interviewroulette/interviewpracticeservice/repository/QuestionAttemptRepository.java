package com.interviewroulette.interviewpracticeservice.repository;

import com.interviewroulette.interviewpracticeservice.entity.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionAttemptRepository
        extends JpaRepository<QuestionAttempt, UUID> {

	// The method retrieves the round's presentation history.
    List<QuestionAttempt> findBySessionIdOrderByAttemptSequenceAsc(UUID sessionId);

    Optional<QuestionAttempt> findByIdAndSessionId(
            UUID id, UUID sessionId
    );

    boolean existsBySessionIdAndQuestionIdAndStatus(
            UUID sessionId, UUID questionId, String status
    );

    int countBySessionId(UUID sessionId);
    

    //This is help us identify a question currently awaiting the candidate's decision.
	Optional<QuestionAttempt> findFirstBySessionIdAndStatus(UUID sessionId, String status);
	

	long countBySessionIdAndStatus(UUID sessionId, String status);

	@Query("""
			SELECT COUNT(DISTINCT qa.questionId)
			FROM QuestionAttempt qa
			WHERE qa.sessionId = :sessionId
			""")
	long countDistinctQuestionsBySessionId(@Param("sessionId") UUID sessionId);

	@Query("""
			SELECT COALESCE(SUM(qa.durationSeconds), 0)
			FROM QuestionAttempt qa
			WHERE qa.sessionId = :sessionId
			""")
	Long getTotalDurationBySessionId(@Param("sessionId") UUID sessionId);

	@Query("""
			SELECT COUNT(qa)
			FROM QuestionAttempt qa
			WHERE qa.sessionId = :sessionId
			  AND qa.durationSeconds IS NOT NULL
			""")
	long countFinishedAttemptsBySessionId(@Param("sessionId") UUID sessionId);
	
	@Query("""
		    SELECT COUNT(qa)
		    FROM QuestionAttempt qa
		    WHERE qa.sessionId IN (
		        SELECT ps.id
		        FROM PracticeSession ps
		        WHERE ps.candidateId = :candidateId
		          AND (:topicId IS NULL OR ps.topicId = :topicId)
		    )
		    """)
		long countCandidateAttempts(
		        @Param("candidateId") UUID candidateId,
		        @Param("topicId") Long topicId
		);

		@Query("""
		    SELECT COUNT(DISTINCT qa.questionId)
		    FROM QuestionAttempt qa
		    WHERE qa.sessionId IN (
		        SELECT ps.id
		        FROM PracticeSession ps
		        WHERE ps.candidateId = :candidateId
		          AND (:topicId IS NULL OR ps.topicId = :topicId)
		    )
		    """)
		long countCandidateUniqueQuestions(
		        @Param("candidateId") UUID candidateId,
		        @Param("topicId") Long topicId
		);

		@Query("""
		    SELECT COUNT(qa)
		    FROM QuestionAttempt qa
		    WHERE qa.status = :status
		      AND qa.sessionId IN (
		          SELECT ps.id
		          FROM PracticeSession ps
		          WHERE ps.candidateId = :candidateId
		            AND (:topicId IS NULL OR ps.topicId = :topicId)
		      )
		    """)
		long countCandidateAttemptsByStatus(
		        @Param("candidateId") UUID candidateId,
		        @Param("topicId") Long topicId,
		        @Param("status") String status
		);

		@Query("""
		    SELECT COALESCE(SUM(qa.durationSeconds), 0)
		    FROM QuestionAttempt qa
		    WHERE qa.sessionId IN (
		        SELECT ps.id
		        FROM PracticeSession ps
		        WHERE ps.candidateId = :candidateId
		          AND (:topicId IS NULL OR ps.topicId = :topicId)
		    )
		    """)
		Long getCandidateTotalDuration(
		        @Param("candidateId") UUID candidateId,
		        @Param("topicId") Long topicId
		);
}