package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.WordExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordExampleRepository extends JpaRepository<WordExample, Long> {
    List<WordExample> findByWordId(Long wordId);
}
