package com.example.languageservice.domain.utils;

import com.example.languageservice.api.dto.ParagraphRequest;
import com.example.languageservice.config.AiModelSettings;
import com.example.languageservice.domain.model.AiPrompt;
import com.example.languageservice.domain.model.AiPromptPayload;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class PromptBuilder {
    private static final AiModelSettings aiModelSettings = new AiModelSettings();
    private static final AiPrompt SYSTEM_PROMPT = AiPrompt.builder()
            .role("system")
            .content("You are a helpful assistant that generates German paragraphs based on user preferences.")
            .build();
    private PromptBuilder() {
        throw new IllegalStateException("Utility class");
    }

    public static AiPromptPayload buildParagraphPrompt(ParagraphRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate a German paragraph.\n")
                .append("Level: ").append(request.getLanguageLevel()).append("\n")
                .append("Length: ").append(request.getLength()).append("\n")
                .append("Topic: ").append(request.getTopic()).append("\n");

        if (request.isIncludeUserWords() && !request.getSelectedWords().isEmpty()) {
            prompt.append("Include these words: ").append(String.join(", ", request.getSelectedWords())).append("\n");
        }

        prompt.append("Respond in JSON format with fields: paragraph, usedWords.");

        if (request.getTranslationLanguage() != null) {
            prompt.append(" Also include translation into ").append(request.getTranslationLanguage()).append(".");
        }

        AiPrompt userPrompt = AiPrompt.builder()
                .role("user")
                .content(prompt.toString())
                .build();

        return AiPromptPayload.builder()
                .model(aiModelSettings.getModel())
                .aiPrompt(List.of(SYSTEM_PROMPT, userPrompt))
                .build();
    }

    public static String buildChatPrompt(String userMessage) {
        return "You are a helpful assistant that translates English to German. "
                + "Translate the following message to German:\n" + userMessage;
    }

    public static String buildUserWordPrompt(List<String> words) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Provide a list of unique German words suitable for language learners. ")
              .append("The words should be common and useful for everyday conversations. ")
              .append("Avoid very technical or specialized terms. ")
              .append("Here are some example words: ").append(String.join(", ", words)).append(". ")
              .append("Return the list in JSON array format.");
        return prompt.toString();
    }
}
