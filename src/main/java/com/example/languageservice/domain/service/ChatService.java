package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.ChatMessageResponse;
import com.example.languageservice.domain.model.ChatMessage;
import com.example.languageservice.domain.repository.ChatMessageRepository;
import com.example.languageservice.domain.repository.SessionRepository;
import com.example.languageservice.security.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final SessionRepository sessionRepository;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String llmApiUrl;
    private final String apiKey;

    public ChatService(ChatMessageRepository chatMessageRepository,
            SessionRepository sessionRepository,
            HttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${llm.api.url}") String llmApiUrl,
            @Value("${llm.api.key}") String apiKey) {
        this.chatMessageRepository = chatMessageRepository;
        this.sessionRepository = sessionRepository;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.llmApiUrl = llmApiUrl;
        this.apiKey = apiKey;
    }

    public ChatMessageResponse sendAndReceiveMessage(UUID sessionId, String userMessage) {
        UUID userId = SecurityUtils.getCurrentUserId();

        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with ID: " + sessionId));

        log.info("Processing chat message for session {} from user {}", sessionId, userId);

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setUserId(userId);
        userMsg.setContent(userMessage);
        userMsg.setRole("user");
        userMsg.setTimestamp(Instant.now());
        chatMessageRepository.save(userMsg);

        List<ChatMessage> chatHistory = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);

        // Prepare chat history for LLM
        String chatHistoryString = chatHistory.stream()
                .map(msg -> String.format("%s: %s", msg.getRole(), msg.getContent()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format("You are a language learning AI. Continue the following conversation with a helpful and encouraging response.\n\n%s\nYou: %s", chatHistoryString, userMessage);

        try {
            String llmResponse = callLlmApi(prompt);

            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSessionId(sessionId);
            aiMsg.setUserId(userId);
            aiMsg.setContent(llmResponse);
            aiMsg.setRole("assistant");
            aiMsg.setTimestamp(Instant.now());
            chatMessageRepository.save(aiMsg);

            log.info("Successfully received and saved AI response for session {}", sessionId);

            return new ChatMessageResponse(llmResponse, Collections.emptyList());

        } catch (Exception e) {
            log.error("Failed to call LLM API for session {}", sessionId, e);
            throw new RuntimeException("Failed to get response from AI.", e);
        }
    }

    @Cacheable("wordEnhancements")
    public ChatMessageResponse enhanceWordWithAI(UUID sessionId, String word) {
        UUID userId = SecurityUtils.getCurrentUserId();

        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with ID: " + sessionId));

        log.info("Enhancing word '{}' for user {}", word, userId);
        String prompt = String.format("Provide a detailed and friendly explanation for the word '%s'. Include synonyms, antonyms, and example sentences.", word);

        try {
            String llmResponse = callLlmApi(prompt);
            log.info("Successfully enhanced word '{}'", word);
            return new ChatMessageResponse(llmResponse, Collections.emptyList());
        } catch (Exception e) {
            log.error("Failed to enhance word '{}' for user {}", word, userId, e);
            throw new RuntimeException("Failed to get AI enhancement.", e);
        }
    }

    private String callLlmApi(String prompt) throws IOException, InterruptedException {
        String jsonPayload = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(llmApiUrl))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("LLM API returned non-200 status: {}", response.statusCode());
            log.error("Response body: {}", response.body());
            throw new RuntimeException("LLM API call failed with status code: " + response.statusCode());
        }

        JsonNode rootNode = objectMapper.readTree(response.body());
        return rootNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
    }

    // Simple placeholder summary until a richer implementation is needed
    public String generateSessionSummary(java.util.List<com.example.languageservice.domain.model.UserInteraction> interactions) {
        int total = interactions != null ? interactions.size() : 0;
        long correct = interactions != null ? interactions.stream().filter(com.example.languageservice.domain.model.UserInteraction::isCorrect).count() : 0;
        return "Total interactions: " + total + ", correct: " + correct + ", incorrect: " + (total - correct);
    }
}
