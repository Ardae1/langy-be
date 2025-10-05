package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.Word;
import com.example.languageservice.domain.model.WordTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WordTranslationRepository extends JpaRepository<WordTranslation, Long> {
    @Query("""
    SELECT wt.targetWord 
    FROM WordTranslation wt 
    JOIN FETCH wt.targetWord t 
    WHERE wt.sourceWord.id = :sourceWordId 
      AND wt.sourceLanguage.code = :sourceLang 
      AND wt.targetLanguage.code = :targetLang
""")
    Optional<Word> findTranslation(Long sourceWordId, String sourceLang, String targetLang);
}
