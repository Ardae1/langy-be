package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.UserWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for the UserWord entity.
 */
@Repository
public interface UserWordRepository extends JpaRepository<UserWord, java.util.UUID> {

    /**
     * Custom query to find words due for review for a specific user. This
     * method directly addresses the performance optimization mentioned in the
     * API flows by fetching only the necessary words with a limit.
     *
     * @param userId The user's ID.
     * @return A list of UserWord entities.
     */
    java.util.Optional<UserWord> findByUserIdAndWord(UUID userId, String word);

    @Query("SELECT uw FROM UserWord uw WHERE uw.userId = :userId AND uw.reviewDate <= :now ORDER BY uw.reviewDate ASC")
    List<UserWord> findWordsForReview(@Param("userId") UUID userId, @Param("now") java.time.Instant now);
}
