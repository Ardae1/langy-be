package com.example.languageservice.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class UserWord {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "box", nullable = false)
    private int box;

    @Column(name = "review_date", nullable = false)
    private Instant reviewDate;
}
