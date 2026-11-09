package com.interviewroulette.interviewpracticeservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "question_attempts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attempt_sequence",
            columnNames = {"session_id", "attempt_sequence"}
        )
    }
)
public class QuestionAttempt {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "id", nullable = false, length = 36)
	private UUID id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "session_id", nullable = false, length = 36)
	private UUID sessionId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "question_id", nullable = false, length = 36)
	private UUID questionId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "attempt_sequence", nullable = false)
    private Integer attemptSequence;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (startedAt == null) {
            startedAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public UUID getQuestionId() { return questionId; }
    public void setQuestionId(UUID questionId) { this.questionId = questionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getAttemptSequence() { return attemptSequence; }
    public void setAttemptSequence(Integer attemptSequence) { this.attemptSequence = attemptSequence; }
}