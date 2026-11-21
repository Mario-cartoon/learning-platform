package com.example.learningplatform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI learningPlatformOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Learning Platform API")
                .version("1.0")
                .description("REST API учебной платформы: курсы, записи, задания, тесты, отзывы"));
    }
}
