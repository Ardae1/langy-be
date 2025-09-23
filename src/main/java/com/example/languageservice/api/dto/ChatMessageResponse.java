package com.example.languageservice.api.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for returning a simple AI response text with optional source
 * attributions.
 */
@Data
public class ChatMessageResponse {

    private String response;
    private List<SourceAttribution> sources;

    public ChatMessageResponse(String response, List<SourceAttribution> sources) {
        this.response = response;
        this.sources = sources;
    }
}
