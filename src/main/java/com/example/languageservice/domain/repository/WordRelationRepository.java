package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.Word;
import com.example.languageservice.domain.model.WordRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRelationRepository extends JpaRepository<WordRelation, Long> {

    @Query("SELECT r.relatedWord FROM WordRelation r WHERE r.word.id = :wordId AND r.relationType = 'SYNONYM'")
    List<Word> findSynonyms(Long wordId);

    @Query("SELECT r.relatedWord FROM WordRelation r WHERE r.word.id = :wordId AND r.relationType = 'ANTONYM'")
    List<Word> findAntonyms(Long wordId);
}
