package com.example.languageservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO for the end session request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndSessionRequest {

    private UUID sessionId;
}
