package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.Paragraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParagraphRepository extends JpaRepository<Paragraph, Long> {
    // Full-text search on paragraph content
    @Query(value = """
            SELECT p.*
            FROM paragraphs p
            WHERE to_tsvector('simple', p.content) @@ to_tsquery(:query)
            LIMIT 5
            """, nativeQuery = true)
    List<Paragraph> searchByContent(@Param("query") String query);
}
