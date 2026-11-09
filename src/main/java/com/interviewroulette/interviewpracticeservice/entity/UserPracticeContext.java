package com.interviewroulette.interviewpracticeservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_practice_context")
public class UserPracticeContext {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "candidate_id", nullable = false, length = 36)
	private UUID candidateId;

	@Column(name = "active_topic_id")
	private Long activeTopicId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "active_session_id", length = 36)
	private UUID activeSessionId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getCandidateId() { return candidateId; }
    public void setCandidateId(UUID candidateId) { this.candidateId = candidateId; }

    public Long getActiveTopicId() { return activeTopicId; }
    public void setActiveTopicId(Long activeTopicId) { this.activeTopicId = activeTopicId; }

    public UUID getActiveSessionId() { return activeSessionId; }
    public void setActiveSessionId(UUID activeSessionId) { this.activeSessionId = activeSessionId; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}