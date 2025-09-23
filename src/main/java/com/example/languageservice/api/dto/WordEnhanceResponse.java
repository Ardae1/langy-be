package com.example.languageservice.api.dto;

import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object for the Word Enhance response.
 */
@Data
public class WordEnhanceResponse {

    private String word;
    private String enhancedContent;
    private List<SourceAttribution> sources;

    public WordEnhanceResponse(String word, String enhancedContent, List<SourceAttribution> sources) {
        this.word = word;
        this.enhancedContent = enhancedContent;
        this.sources = sources;
    }
}
