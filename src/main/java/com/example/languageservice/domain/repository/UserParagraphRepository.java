package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.Paragraph;
import com.example.languageservice.domain.model.UserParagraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserParagraphRepository extends JpaRepository<UserParagraph, Long> {

    List<UserParagraph> findByUserId(UUID userId);

    // Superset query on user_paragraphs.requested_words
    @Query(value = """
            SELECT p.* 
            FROM paragraphs p
            JOIN user_paragraphs up ON p.id = up.paragraph_id
            WHERE up.requested_words @> :words
            LIMIT 5
            """, nativeQuery = true)
    List<Paragraph> findSupersetMatch(@Param("words") String[] words);

}
