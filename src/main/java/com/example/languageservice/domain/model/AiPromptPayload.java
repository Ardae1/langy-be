package com.example.languageservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AiPromptPayload {
    private  String model;
    private List<AiPrompt> aiPrompt;
    private final double temperature = 0.7;
}
