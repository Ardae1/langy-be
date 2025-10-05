package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.WordForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordFormRepository extends JpaRepository<WordForm, Long> {
    List<WordForm> findByWordId(Long wordId);
    Optional<WordForm> findByForm(String form);
}
