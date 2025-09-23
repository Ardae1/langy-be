package com.example.languageservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single message in an AI chat session. Maps to the
 * 'chat_messages' table.
 */
@Entity
@Table(name = "chat_messages")
@Data
public class ChatMessage {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "role", nullable = false)
    private String role; // 'user' or 'assistant'

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}
