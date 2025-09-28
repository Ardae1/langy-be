package com.example.languageservice.api.dto;

import com.example.languageservice.domain.model.LanguageLevel;
import com.example.languageservice.domain.model.ParagraphLength;
import lombok.Data;

import java.util.List;
@Data
public class ParagraphRequest {
    private String topic;
    private LanguageLevel languageLevel;
    private ParagraphLength length;
    private boolean includeUserWords;
    private List<String> selectedWords;
    private String translationLanguage;
}