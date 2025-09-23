package com.example.languageservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for starting a session, carrying only the target language code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionRequest {

    private String language;
}
