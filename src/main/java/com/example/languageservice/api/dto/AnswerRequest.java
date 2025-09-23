package com.example.languageservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO for submitting a word answer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {

    private UUID sessionId;
    private String word;
    private boolean correct;
}
