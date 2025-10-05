package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "word_relations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_word_id")
    private Word relatedWord;

    private String relationType; // SYNONYM / ANTONYM
}
