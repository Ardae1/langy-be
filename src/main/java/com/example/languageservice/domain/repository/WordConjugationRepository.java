package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.WordConjugation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordConjugationRepository extends JpaRepository<WordConjugation, Long> {
    List<WordConjugation> findByWordId(Long wordId);
    Optional<WordConjugation> findByForm(String form);
}
