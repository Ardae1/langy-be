package com.example.languageservice.api.dto;

import lombok.Data;

/**
 * Data Transfer Object for a single source attribution from the LLM.
 */
@Data
public class SourceAttribution {

    private String uri;
    private String title;

    public SourceAttribution(String uri, String title) {
        this.uri = uri;
        this.title = title;
    }
}
