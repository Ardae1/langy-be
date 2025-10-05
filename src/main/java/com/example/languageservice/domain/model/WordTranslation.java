package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;


@Entity
@Table(name = "word_translations",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "source_word_id", "target_word_id", "source_language_id", "target_language_id"
        }),
        indexes = {
                @Index(name = "idx_source_lang", columnList = "source_language_id"),
                @Index(name = "idx_target_lang", columnList = "target_language_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordTranslation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_word_id", nullable = false)
    private Word sourceWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_word_id", nullable = false)
    private Word targetWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_language_id", nullable = false)
    private Language sourceLanguage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_language_id", nullable = false)
    private Language targetLanguage;

    // --- Metadata ---
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
