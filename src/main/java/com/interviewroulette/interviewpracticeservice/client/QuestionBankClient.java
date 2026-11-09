package com.interviewroulette.interviewpracticeservice.client;

import com.interviewroulette.interviewpracticeservice.dto.questionbank.QuestionResponse;
import com.interviewroulette.interviewpracticeservice.dto.questionbank.TopicResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import java.util.function.Supplier;

@Component
public class QuestionBankClient {

    private final RestClient restClient;

    public QuestionBankClient(RestClient questionBankRestClient) {
        this.restClient = questionBankRestClient;
    }

    public List<TopicResponse> getActiveTopics() {
        return execute(() -> restClient.get()
                .uri("/api/v1/topics")
                .retrieve()
                .body(new ParameterizedTypeReference<List<TopicResponse>>() {}));
    }

    public TopicResponse getTopic(Long topicId) {
        return execute(() -> restClient.get()
                .uri("/api/v1/topics/{topicId}", topicId)
                .retrieve()
                .body(TopicResponse.class));
    }

    public List<QuestionResponse> getQuestionsByTopic(Long topicId) {
        return execute(() -> restClient.get()
                .uri("/api/v1/topics/{topicId}/questions", topicId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<QuestionResponse>>() {}));
    }

    public QuestionResponse getQuestion(UUID questionId) {
        return execute(() -> restClient.get()
                .uri("/api/v1/questions/{questionId}", questionId)
                .retrieve()
                .body(QuestionResponse.class));
    }

    public List<QuestionResponse> getCandidateQuestionsByTopic(
            UUID candidateId,
            Long topicId
    ) {
        return execute(() -> restClient.get()
                .uri(
                        "/api/v1/candidates/{candidateId}/topics/{topicId}/questions",
                        candidateId,
                        topicId
                )
                .retrieve()
                .body(new ParameterizedTypeReference<List<QuestionResponse>>() {}));
    }

    private <T> T execute(Supplier<T> request) {

        try {
            return request.get();

        } catch (RestClientResponseException ex) {

//            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());

            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Requested resource was not found in Question Bank",
                        ex
                );
            }

            if (ex.getStatusCode().value() == 503) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Question Bank service is temporarily unavailable",
                        ex
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Question Bank service returned an unexpected response",
                    ex
            );

        } catch (ResourceAccessException ex) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to connect to Question Bank service",
                    ex
            );

        } catch (RestClientException ex) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Error communicating with Question Bank service",
                    ex
            );
        }
    }
}