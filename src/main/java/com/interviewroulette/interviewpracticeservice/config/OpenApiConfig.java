package com.interviewroulette.interviewpracticeservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI interviewRouletteOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Interview Roulette - Practice Service API")
                        .description(
                                "REST APIs for interview practice sessions, " +
                                "question selection, attempts, history and statistics."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Interview Roulette")
                                .email("support@interviewroulette.com")
                        )
                );
    }
}