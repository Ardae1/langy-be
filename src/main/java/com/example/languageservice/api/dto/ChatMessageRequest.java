package com.example.languageservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO for the chat message request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    private UUID sessionId;
    private String message;
}
