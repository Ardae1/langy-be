package com.example.languageservice.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class UserRandomWordRequest {
    private String sourceLanguage; // e.g. "en"
    private String level;  // e.g. "A1"
    private int count;     // e.g. 5, 10, 20


}
