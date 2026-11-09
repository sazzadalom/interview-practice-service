# interview-practice-service

Interview Roulette **Practice Service** (port `9091`): owns practice sessions, question attempts, completion/skip transitions, elapsed-time calculation, practice progress, dashboard statistics and practice history.

The service works with the Question Bank Service (port `9090`) to obtain eligible questions. The browser does not decide which question is eligible and does not calculate authoritative solving time.

**Technology stack:** Java 21, Spring Boot 3.x, Spring Web, Spring Data JPA, Hibernate, MySQL 8, Maven, Jakarta Validation, Lombok (where used), JUnit 5, Mockito and Docker.

> **Contract note:** This README documents the established application responsibilities and the practice API contract. The original controller source was not included with the README sample, so verify the exact route annotations and DTO property names against the current `@RequestMapping` / `@PostMapping` declarations before treating every path below as a generated OpenAPI contract. The request and response bodies are representative contract examples, not claims that every optional field is present in the current DTO.

## Package layout

The service is organised by responsibility using a layered Spring Boot structure. The package names below describe the logical modules; retain the concrete names used in the source tree if they differ.

```text
com.interviewroulette.practice
 ├── config          Database, JPA, Jackson, CORS and application configuration
 ├── controller      Practice/session, attempt, dashboard and history REST controllers
 ├── dto             Request/response DTOs; session, attempt, summary and statistics models
 ├── entity          Candidate/user reference, PracticeSession, QuestionAttempt and enums
 ├── repository      Spring Data JPA repositories and query methods
 ├── service         Session lifecycle, question selection orchestration, attempt and dashboard services
 ├── client           HTTP client / adapter for interview-question-bank-service
 ├── exception       Domain exceptions and global REST exception handling
 └── InterviewPracticeServiceApplication.java
```

## Purpose and responsibilities

The Practice Service is the owner of a candidate's practice activity. It records *what the candidate did*, while the Question Bank Service owns *what questions exist*.

Responsibilities:

- Start a practice session for a candidate and selected topic.
- Maintain session and round state.
- Ask the Question Bank Service for the next eligible question.
- Create and maintain question-attempt records.
- Mark attempts as completed or skipped.
- Calculate solving duration using backend timestamps.
- Prevent completed questions from being repeated in the same round.
- Preserve attempt history across rounds.
- Return progress, dashboard statistics and practice history.

The service does not store a second copy of the central question bank. It stores question identifiers and attempt metadata and obtains question content from the Question Bank Service when required.

## Architecture and approach

```text
React Frontend
    |
    | REST / JSON
    v
Practice Service :9091
    |-- practice database
    |     |-- practice_sessions
    |     |-- question_attempts
    |     `-- candidate/session context as implemented
    |
    `---- REST client ----> Question Bank Service :9090
                              |
                              `---- question-bank database
```

### Why separate the Practice Service?

- **Clear ownership:** practice lifecycle and attempt history are isolated from question catalogue and import logic.
- **Independent scaling:** question reads/import workloads and practice writes can be scaled independently.
- **Smaller transactions:** session and attempt updates do not need to own question-bank transactions.
- **Maintainability:** changes to dashboards or attempt lifecycle need not change the question catalogue.
- **Data integrity:** practice state is persisted in the service that owns it.

### Practice lifecycle

1. The frontend submits a candidate ID and topic ID to start practice.
2. The service creates or resolves the active practice session/round according to the implemented session rules.
3. When the frontend asks for a question, the service obtains an eligible question from the Question Bank Service.
4. The service creates an attempt and records `startedAt` on the server.
5. The frontend displays the question and a live elapsed-time counter.
6. On Complete, the service records `completedAt` and calculates duration from server timestamps.
7. On Skip, the attempt is marked skipped; it is not treated as a completed question.
8. The next eligible question is requested.
9. When no eligible questions remain, the round/session is completed and a summary is returned.

### Why server-side timing?

The browser timer is a presentation feature. It can be paused by browser suspension, manipulated by a user, or reset by refreshing the page. The server's `startedAt` and `completedAt` timestamps are authoritative, so the stored duration remains consistent across refreshes and devices.

Example:

```text
startedAt   = 2026-11-10T10:00:00Z
completedAt = 2026-11-10T10:08:45Z
duration    = 525 seconds
```

### Attempt state

| Status | Meaning |
| --- | --- |
| `ASSIGNED` | Question is currently assigned to the candidate |
| `COMPLETED` | Candidate marked the question as completed |
| `SKIPPED` | Candidate skipped it; it is not counted as completed |
| `ABANDONED` | Attempt/session was ended without completion, if supported by the current implementation |

A completed question is excluded from selection for the same round. A skipped question remains eligible according to the question-selection rules. A new round is a separate practice record; prior attempts are retained.

## Database model

The Practice Service owns the practice-side data. The logical core is:

| Table | Purpose |
| --- | --- |
| `practice_sessions` | Candidate, topic, round/session status and start/end timestamps |
| `question_attempts` | Question reference, attempt status, start/end timestamps and duration |
| Candidate/context table (if present) | Current candidate/topic/session context |

Important relationships:

- One candidate can have multiple practice sessions.
- One session has multiple question attempts.
- Each attempt references a question ID owned by the Question Bank Service.
- The practice database does not need to duplicate question text to preserve attempt history.

Use foreign keys for relationships within this database, indexes for candidate/session/status lookups, and a unique or conditional-write strategy where the application must prevent more than one active attempt for a session.

## Run it

1. Install Java 21 and Maven.
2. Create the Practice Service MySQL database and database user.
3. Configure the database connection.
4. Start the Question Bank Service on port `9090`.
5. Configure the Question Bank base URL used by the practice service.
6. Start this service.

```bash
mvn clean package
mvn spring-boot:run
mvn test
```

If the repository contains a Docker Compose file:

```bash
docker compose up --build
docker build \                                             
  --build-arg VITE_QUESTION_BANK_API_URL=http://localhost:9090 \
  --build-arg VITE_PRACTICE_API_URL=http://localhost:9091 \
  -t interview-roulette-frontend:1.0 .

```

### Configuration

The exact property names/defaults must be read from this service's `application.yml` or `application.properties`. The following values describe the required configuration concepts; do not assume the example environment variable names are already wired into Spring configuration.

| Configuration | Local value / example | Purpose |
| --- | --- | --- |
| Server port | `9091` | Practice Service HTTP port |
| Database URL | `jdbc:mysql://localhost:3306/interview_practice_db` | Practice database |
| Database username/password | local development credentials | Database connection |
| Question Bank base URL | `http://localhost:9090` | Internal REST calls for question retrieval |
| JPA DDL mode | project configuration | Schema generation/validation policy |
| Logging level | project configuration | Application diagnostics |

Never commit production database credentials. Use environment variables or a secret manager in deployed environments.

## Endpoints

The following endpoint list represents the practice API capabilities used by the product. Confirm the literal paths and any naming differences against the current controller mappings before publishing an external API contract.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/practice/sessions` | Start a practice session for a candidate and topic |
| `GET` | `/api/v1/practice/sessions/{sessionId}` | Retrieve session details/status |
| `GET` | `/api/v1/practice/sessions/{sessionId}/next-question` | Assign or retrieve the next eligible question |
| `POST` | `/api/v1/practice/attempts/{attemptId}/complete` | Complete the active question attempt |
| `POST` | `/api/v1/practice/attempts/{attemptId}/skip` | Skip the active question attempt |
| `POST` | `/api/v1/practice/sessions/{sessionId}/complete` | Complete/end a practice session |
| `POST` | `/api/v1/practice/sessions/{sessionId}/next-round` | Start another round for the same topic, if exposed |
| `GET` | `/api/v1/practice/candidates/{candidateId}/dashboard` | Retrieve practice statistics |
| `GET` | `/api/v1/practice/candidates/{candidateId}/history` | Retrieve previous sessions and attempts |

### 1. Start a practice session

`POST /api/v1/practice/sessions`

Request:

```json
{
  "candidateId": "6a305976-38a1-4310-a323-fda449837d49",
  "topicId": 1
}
```

Representative response (`201 Created`):

```json
{
  "sessionId": "c8f2d04e-95a8-4ed6-9d1f-5a3c5c2f90e1",
  "candidateId": "6a305976-38a1-4310-a323-fda449837d49",
  "topicId": 1,
  "topicName": "Core Java",
  "roundNumber": 1,
  "status": "ACTIVE",
  "startedAt": "2026-11-10T10:00:00Z"
}
```

The frontend stores the returned session object locally to restore the active screen after navigation or refresh. The database remains the source of truth.

### 2. Get session details

`GET /api/v1/practice/sessions/{sessionId}`

Representative response (`200 OK`):

```json
{
  "sessionId": "c8f2d04e-95a8-4ed6-9d1f-5a3c5c2f90e1",
  "candidateId": "6a305976-38a1-4310-a323-fda449837d49",
  "topicId": 1,
  "topicName": "Core Java",
  "roundNumber": 1,
  "status": "ACTIVE",
  "startedAt": "2026-11-10T10:00:00Z",
  "completedAt": null
}
```

### 3. Get the next question

`GET /api/v1/practice/sessions/{sessionId}/next-question`

Representative response (`200 OK`):

```json
{
  "sessionId": "c8f2d04e-95a8-4ed6-9d1f-5a3c5c2f90e1",
  "attemptId": "c2c69e4e-39a1-47bb-a20c-9fd3b3e87f01",
  "questionId": "e0a36c15-7f4c-4e67-a8b5-2d25c4f1c111",
  "questionText": "What is the difference between HashMap and Hashtable?",
  "topicName": "Core Java",
  "difficulty": "INTERMEDIATE",
  "startedAt": "2026-11-10T10:02:00Z",
  "status": "ASSIGNED"
}
```

When no eligible question remains, the service should return the implemented round-completion representation rather than inventing a question. The exact empty/completed response depends on the controller DTO.

### 4. Complete an attempt

`POST /api/v1/practice/attempts/{attemptId}/complete`

No client-provided duration is required. The backend uses the stored `startedAt` and its own current time.

Representative response (`200 OK`):

```json
{
  "attemptId": "c2c69e4e-39a1-47bb-a20c-9fd3b3e87f01",
  "status": "COMPLETED",
  "startedAt": "2026-11-10T10:02:00Z",
  "completedAt": "2026-11-10T10:08:45Z",
  "solvingTimeSeconds": 405
}
```

### 5. Skip an attempt

`POST /api/v1/practice/attempts/{attemptId}/skip`

Representative response (`200 OK`):

```json
{
  "attemptId": "c2c69e4e-39a1-47bb-a20c-9fd3b3e87f01",
  "status": "SKIPPED",
  "startedAt": "2026-11-10T10:02:00Z"
}
```

A skipped attempt is recorded for history but is not counted as completed.

### 6. Complete a session

`POST /api/v1/practice/sessions/{sessionId}/complete`

Representative response (`200 OK`):

```json
{
  "sessionId": "c8f2d04e-95a8-4ed6-9d1f-5a3c5c2f90e1",
  "status": "COMPLETED",
  "completedAt": "2026-11-10T11:00:00Z"
}
```

### 7. Dashboard statistics

`GET /api/v1/practice/candidates/{candidateId}/dashboard`

Representative response (`200 OK`):

```json
{
  "candidateId": "6a305976-38a1-4310-a323-fda449837d49",
  "totalQuestions": 500,
  "completedQuestions": 125,
  "skippedQuestions": 15,
  "remainingQuestions": 375,
  "totalSolvingTimeSeconds": 34350,
  "averageSolvingTimeSeconds": 275,
  "sessionCount": 4,
  "completionPercentage": 25.0
}
```

The numbers above are examples only.

### 8. Practice history

`GET /api/v1/practice/candidates/{candidateId}/history`

Representative response (`200 OK`):

```json
{
  "candidateId": "6a305976-38a1-4310-a323-fda449837d49",
  "items": [
    {
      "sessionId": "c8f2d04e-95a8-4ed6-9d1f-5a3c5c2f90e1",
      "topicName": "Core Java",
      "roundNumber": 1,
      "questionText": "What is the difference between HashMap and Hashtable?",
      "status": "COMPLETED",
      "solvingTimeSeconds": 405,
      "attemptedAt": "2026-11-10T10:02:00Z"
    }
  ]
}
```

Pagination fields should be documented here if the current controller uses a paged response.

## How the important rules are implemented

- **Backend owns time.** The client timer is visual only; completion duration is calculated from server timestamps.
- **Completed questions do not repeat in the same round.** Attempt status is part of the eligibility check.
- **Skipped is not completed.** A skip is preserved as an attempt state and remains eligible under the question-selection policy.
- **History is append-oriented.** New rounds create new session/attempt records; old records are not overwritten.
- **Question ownership remains separate.** The Question Bank Service owns question text and contributions; the Practice Service owns attempt lifecycle.
- **One active attempt at a time.** The service should enforce this rule transactionally if it is part of the current implementation.
- **Errors should be consistent.** Validation, missing session/attempt, invalid state transition and downstream Question Bank failures should map to appropriate HTTP status codes without exposing stack traces.

## Security and cryptographic considerations

The current Interview Roulette MVP uses candidate UUIDs and local browser storage for candidate/session continuity and does not have email/password authentication or a confirmed encryption layer for candidate identifiers. Do not describe UUIDs as encrypted values.

The Question Bank Service uses SHA-256 for normalized-question duplicate detection. This is a hash, not encryption; see the Question Bank README for the algorithm and rationale.

For production, use HTTPS/TLS for data in transit, protect database credentials, apply database-at-rest encryption or managed disk encryption where required, and introduce authentication/authorization before exposing candidate-specific endpoints publicly.

## Verification status

- The application was previously run locally with the Practice Service on port `9091`.
- The frontend integrates with the practice service through its Axios-based practice API module.
- This README's example response bodies are illustrative. Verify each endpoint path and DTO field against the current controllers and service DTOs.
- Run `mvn test` and exercise the session lifecycle against a local MySQL database before release.
