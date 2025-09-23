package com.example.languageservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.UUID;

/**
 * Represents a user's interaction within a session. Maps to the
 * 'user_interactions' table.
 */
@Entity
@Table(name = "user_interactions")
@Data
public class UserInteraction {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    // No-args constructor for service usage
    public UserInteraction() {
    }

    // Compatibility constructor used by tests
    public UserInteraction(UUID id, UUID sessionId, String word, String userAnswer, String correctAnswer, boolean correct) {
        this.id = id;
        this.sessionId = sessionId;
        this.word = word;
        this.correct = correct;
    }
}
