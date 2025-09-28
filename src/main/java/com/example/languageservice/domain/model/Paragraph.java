package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "paragraphs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Paragraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LanguageLevel level;   // A1, B1, etc.
    private String topic;   // e.g., Daily Life
    private ParagraphLength length;  // SHORT, MEDIUM, LONG

    private Instant createdAt = Instant.now();
}
