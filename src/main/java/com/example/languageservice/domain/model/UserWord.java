package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a word associated with a user for the Spaced Repetition System.
 */
@Entity
@Table(name = "user_words")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWord {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    private LanguageLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private Box box;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;
    @Column(name = "accuracy", nullable = false)
    private double accuracy;

    @Column(name = "created_at", nullable = false)
    private double createdAt;
}
