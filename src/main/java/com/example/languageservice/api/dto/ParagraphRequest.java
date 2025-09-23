package com.example.languageservice.api.dto;

import lombok.Data;

import java.util.List;
@Data
public class ParagraphRequest {
    private String topic;
    private String difficulty;
    private String length;
    private int wordCount;
    private boolean includeUserWords;
    private List<String> vocabulary;
    private String translationLanguage;
    private Integer seed;
}