package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.UserWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the UserWord entity.
 */

// queries should be tailored for language based search.
@Repository
public interface UserWordRepository extends JpaRepository<UserWord, Long> {
    boolean existsByUserIdAndWordId(UUID userId, Long wordId);

    List<UserWord> findByUserId(UUID userId);
    @Query("""
        SELECT uw FROM UserWord uw
        WHERE uw.user.id = :userId
        AND uw.lastSeen + b.intervalDays * 1 DAY <= :now
        """)
    List<UserWord> findDueWords(@Param("userId") UUID userId,
                                     @Param("now") LocalDateTime now);
    @Query("SELECT uw FROM UserWord uw WHERE uw.user.id = :userId AND uw.word.id = :wordId")
    Optional<UserWord> findByUserIdAndWordId(UUID userId, Long wordId);

    @Query("SELECT uw FROM UserWord uw WHERE uw.user.id = :userId AND uw.box.id = :boxId")
    List<UserWord> findByUserAndBox(@Param("userId") Long userId, @Param("boxId") Long boxId);
}
