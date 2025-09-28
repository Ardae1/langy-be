package com.example.languageservice.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Getter
@Configuration
@ConfigurationProperties(prefix = "llm")
public final class AiModelSettings {
    private String baseUrl;
    private String model;
}
