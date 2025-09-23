package com.example.languageservice.domain.service;

import com.example.languageservice.api.dto.ParagraphRequest;
import com.example.languageservice.api.dto.ParagraphResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//currently data is mocked ->  should be integrated with an actual LLM (Gemma 2b) to generate paragraphs based on the request.
//based on response from gemma 2b, buffer mechanism might be needed.
//remove validation from service and do it in controller.
@Service
public class ParagraphService {

    public ParagraphResponse generateParagraph(ParagraphRequest request) {
        validateRequest(request);

        // Clamp wordCount based on length
        int minWords = getMinWords(request.getLength());
        int maxWords = getMaxWords(request.getLength());
        int wordCount = Math.max(minWords, Math.min(request.getWordCount(), maxWords));

        // Generate paragraph (mock implementation)
        String text = "Generated German paragraph based on the topic and difficulty.";
        String translation = "Translation of the generated paragraph.";
        List<String> usedWords = new ArrayList<>(request.getVocabulary() != null ? request.getVocabulary() : List.of());

        return new ParagraphResponse(text, translation, usedWords, request.getDifficulty(), request.getTopic(), wordCount);
    }

    private void validateRequest(ParagraphRequest request) {
        int minWords = getMinWords(request.getLength());
        int maxWords = getMaxWords(request.getLength());

        if (request.getWordCount() < minWords || request.getWordCount() > maxWords) {
            throw new IllegalArgumentException("wordCount must be between " + minWords + " and " + maxWords + " for length=" + request.getLength());
        }
    }

    private int getMinWords(String length) {
        return switch (length) {
            case "short" -> 50;
            case "medium" -> 100;
            case "long" -> 200;
            default -> throw new IllegalArgumentException("Invalid length: " + length);
        };
    }

    private int getMaxWords(String length) {
        return switch (length) {
            case "short" -> 100;
            case "medium" -> 200;
            case "long" -> 300;
            default -> throw new IllegalArgumentException("Invalid length: " + length);
        };
    }
}{
}
