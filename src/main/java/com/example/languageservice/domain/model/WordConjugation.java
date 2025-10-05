package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "word_conjugations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordConjugation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String person; // ich, du, er/sie/es...
    private String form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;
}
