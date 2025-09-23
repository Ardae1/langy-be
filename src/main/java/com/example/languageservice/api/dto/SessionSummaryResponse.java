package com.example.languageservice.api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for the Session Summary response.
 */
@Data
public class SessionSummaryResponse {

    private UUID sessionId;
    private String summary;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public SessionSummaryResponse(UUID sessionId, String summary, LocalDateTime startedAt, LocalDateTime endedAt) {
        this.sessionId = sessionId;
        this.summary = summary;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }
}
