package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "word_forms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @Column(nullable = false)
    private String type; // V2, V3, FUTURE
    @Column(nullable = false)
    private String form;
}
