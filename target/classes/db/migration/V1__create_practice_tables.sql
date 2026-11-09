CREATE TABLE practice_sessions (
    id CHAR(36) NOT NULL,
    candidate_id CHAR(36) NOT NULL,
    topic_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    round_number INT NOT NULL DEFAULT 1,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    PRIMARY KEY (id),
    KEY idx_practice_candidate (candidate_id),
    KEY idx_practice_topic (topic_id),
    KEY idx_practice_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE question_attempts (
    id CHAR(36) NOT NULL,
    session_id CHAR(36) NOT NULL,
    question_id CHAR(36) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    duration_seconds BIGINT NULL,
    attempt_sequence INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attempt_sequence (session_id, attempt_sequence),
    KEY idx_attempt_session (session_id),
    KEY idx_attempt_question (question_id),
    KEY idx_attempt_status (status),
    CONSTRAINT fk_attempt_session
        FOREIGN KEY (session_id) REFERENCES practice_sessions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE user_practice_context (
    candidate_id CHAR(36) NOT NULL,
    active_topic_id BIGINT NULL,
    active_session_id CHAR(36) NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (candidate_id),
    KEY idx_context_active_session (active_session_id),
    CONSTRAINT fk_context_active_session
        FOREIGN KEY (active_session_id) REFERENCES practice_sessions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;