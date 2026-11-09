package com.interviewroulette.interviewpracticeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


/*
 * This uses Spring's RestClient for synchronous HTTP calls.
 * */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient questionBankRestClient(
            RestClient.Builder builder,
            QuestionBankProperties properties
    ) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }
}