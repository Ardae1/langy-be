package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for the UserInteraction entity.
 */
@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, java.util.UUID> {

    /**
     * Custom query to find all interactions for a specific session.
     *
     * @param sessionId The ID of the session.
     * @return A list of user interactions.
     */
    List<UserInteraction> findBySessionId(UUID sessionId);
}
