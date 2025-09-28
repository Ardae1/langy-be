package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_paragraphs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserParagraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paragraph_id", nullable = false)
    private Paragraph paragraph;

    @ElementCollection
    @CollectionTable(name = "user_paragraph_words", joinColumns = @JoinColumn(name = "user_paragraph_id"))
    @Column(name = "word")
    private List<String> requestedWords;

    private boolean seen = false;
    private int refreshedCount = 0;

    private Instant createdAt = Instant.now();
}





