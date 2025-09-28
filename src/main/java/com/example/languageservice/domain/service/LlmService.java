package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.ParagraphResponse;
import com.example.languageservice.config.AiModelSettings;
import com.example.languageservice.domain.model.AiPromptPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlmService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AiModelSettings aiModelSettings;

    public LlmService(ObjectMapper objectMapper, AiModelSettings aiModelSettings) {
        this.aiModelSettings = aiModelSettings;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
    }

    public ParagraphResponse generateContent(AiPromptPayload payload) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiModelSettings.getBaseUrl() + "/chat/completions"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("LLM API call failed: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("choices").get(0).path("message").path("content").asText();

        // Ensure only JSON is returned → map directly
        return objectMapper.readValue(content, ParagraphResponse.class);
    }
}
