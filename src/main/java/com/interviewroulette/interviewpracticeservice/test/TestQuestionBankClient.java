/*package com.interviewroulette.interviewpracticeservice.test;

import com.interviewroulette.interviewpracticeservice.client.QuestionBankClient;
import com.interviewroulette.interviewpracticeservice.dto.questionbank.QuestionResponse;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TestQuestionBankClient implements CommandLineRunner {

    private final QuestionBankClient questionBankClient;

    public TestQuestionBankClient(QuestionBankClient questionBankClient) {
        this.questionBankClient = questionBankClient;
    }

    @Override
    public void run(String... args) {

        UUID candidateId = UUID.fromString(
                "550e8400-e29b-41d4-a716-446655440000"
        );

        Long topicId = 1L;

        List<QuestionResponse> questions =
                questionBankClient.getCandidateQuestionsByTopic(
                        candidateId,
                        topicId
                );

        System.out.println("=================================");
        System.out.println("UPLOADED QUESTIONS TEST");
        System.out.println("Total questions: " + questions.size());

        questions.forEach(question ->
                System.out.println(
                        question.id() + " | " +
                        question.questionText()
                )
        );

        System.out.println("=================================");
    }
}*/