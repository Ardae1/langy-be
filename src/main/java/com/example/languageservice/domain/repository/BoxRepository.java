package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.Box;
import com.example.languageservice.domain.model.BoxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoxRepository extends JpaRepository<Box, Integer> {
    Optional<Box> findByType(BoxType type);
}
