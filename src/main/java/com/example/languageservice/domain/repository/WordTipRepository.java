package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.WordTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordTipRepository extends JpaRepository<WordTip, Long> {
    List<WordTip> findByWordId(Long wordId);
}
