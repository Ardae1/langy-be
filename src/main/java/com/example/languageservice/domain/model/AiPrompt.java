package com.example.languageservice.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiPrompt {
    private String role;
    private String content;
}
