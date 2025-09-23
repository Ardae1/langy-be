package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for the Session entity. Spring Data JPA automatically
 * provides a complete implementation of this interface, offering methods like
 * save(), findById(), and findAll() without writing any code.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * Custom query method to find sessions by user ID. Spring Data JPA creates
     * the SQL query automatically based on the method name.
     *
     * @param userId The ID of the user.
     * @return A list of sessions belonging to the user.
     */
    List<Session> findByUserIdOrderByStartedAtDesc(UUID userId);
}
