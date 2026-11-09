package com.interviewroulette.interviewpracticeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services.question-bank")
public record QuestionBankProperties(String baseUrl) {

}