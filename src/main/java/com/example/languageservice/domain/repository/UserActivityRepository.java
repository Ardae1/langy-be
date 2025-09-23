package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {
    List<UserActivity> findBySessionIdOrderByTimestampAsc(UUID sessionId);
}