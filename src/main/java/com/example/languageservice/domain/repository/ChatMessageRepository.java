package com.example.languageservice.domain.repository;

import com.example.languageservice.domain.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for the ChatMessage entity.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, java.util.UUID> {

    /**
     * Finds all messages for a given session, ordered by creation time. This is
     * crucial for maintaining the context of a conversation for the LLM.
     *
     * @param sessionId The ID of the chat session.
     * @return A list of chat messages.
     */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(UUID sessionId);
}
