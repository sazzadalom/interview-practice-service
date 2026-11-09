package com.interviewroulette.interviewpracticeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.interviewroulette.interviewpracticeservice.config.QuestionBankProperties;

@SpringBootApplication
@EnableConfigurationProperties(QuestionBankProperties.class)
public class InterviewPracticeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewPracticeServiceApplication.class, args);
	}

}
