package com.example.languageservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a learning session. Maps to the 'sessions' table.
 */
@Entity
@Table(name = "sessions")
@Data
public class Session {

    @Id
    @Column(name = "id")
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;
}
