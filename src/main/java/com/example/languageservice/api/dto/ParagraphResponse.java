package com.example.languageservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
public class ParagraphResponse {
    private String text;
    private String translation;
    private List<String> usedWords;
    private String difficulty;
    private String topic;
}