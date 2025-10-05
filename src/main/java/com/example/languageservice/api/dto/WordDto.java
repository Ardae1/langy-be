package com.example.languageservice.api.dto;

import com.example.languageservice.domain.model.Language;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class WordDto {
    private Long id;
    private String word;
    private String language;
    private String level;
    private String partOfSpeech;
    private List<String> forms;
    private Map<String, String> conjugations;
    private List<String> examples;
    private List<String> tips;
    private List<String> synonyms;
    private List<String> antonyms;
}
