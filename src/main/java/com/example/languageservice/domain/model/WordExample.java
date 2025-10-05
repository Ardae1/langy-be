package com.example.languageservice.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "word_examples")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    private String formType; // e.g. "V2", "V3", "PRESENT_ICH"
    @Column(columnDefinition = "TEXT")
    private String sentence;
}
