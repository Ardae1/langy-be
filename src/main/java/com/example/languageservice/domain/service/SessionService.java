package com.example.languageservice.domain.service;

import com.example.languageservice.domain.model.Session;
import com.example.languageservice.domain.model.UserInteraction;
import com.example.languageservice.domain.model.UserWord;
import com.example.languageservice.domain.repository.SessionRepository;
import com.example.languageservice.domain.repository.UserInteractionRepository;
import com.example.languageservice.domain.repository.UserWordRepository;
import com.example.languageservice.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final UserWordRepository userWordRepository;
    private final ChatService chatService;

    public SessionService(SessionRepository sessionRepository,
            UserInteractionRepository userInteractionRepository,
            UserWordRepository userWordRepository,
            ChatService chatService) {
        this.sessionRepository = sessionRepository;
        this.userInteractionRepository = userInteractionRepository;
        this.userWordRepository = userWordRepository;
        this.chatService = chatService;
    }

    @Transactional
    public Session startSession(String language) {
        UUID userId = SecurityUtils.getCurrentUserId();
        log.info("Starting new session for user {} with language {}", userId, language);
        Session session = new Session();
        session.setUserId(userId);
        session.setLanguage(language);
        session.setStartedAt(Instant.now());
        session.setCompleted(false);
        return sessionRepository.save(session);
    }

    @Transactional
    public void submitWordAnswer(UUID sessionId, String word, boolean isCorrect) {
        UUID userId = SecurityUtils.getCurrentUserId();
        log.info("User {} submitted answer for word '{}' in session {}", userId, word, sessionId);

        Optional<UserWord> optionalUserWord = userWordRepository.findByUserIdAndWord(userId, word);
        UserWord userWord = optionalUserWord.orElseGet(() -> {
            UserWord uw = new UserWord();
            uw.setId(java.util.UUID.randomUUID());
            uw.setUserId(userId);
            uw.setWord(word);
            uw.setBox(1);
            uw.setReviewDate(Instant.now());
            return uw;
        });

        int newBox = userWord.getBox() + (isCorrect ? 1 : -1);
        if (newBox < 1) {
            newBox = 1;
        }

        userWord.setBox(newBox);
        userWord.setReviewDate(calculateNextReviewDate(newBox));

        userWordRepository.save(userWord);

        UserInteraction interaction = new UserInteraction();
        interaction.setId(java.util.UUID.randomUUID());
        interaction.setSessionId(sessionId);
        interaction.setUserId(userId);
        interaction.setWord(word);
        interaction.setCorrect(isCorrect);
        userInteractionRepository.save(interaction);
    }

    private Instant calculateNextReviewDate(int box) {
        // Simple spaced repetition algorithm
        switch (box) {
            case 1:
                return Instant.now().plus(1, ChronoUnit.DAYS);
            case 2:
                return Instant.now().plus(3, ChronoUnit.DAYS);
            case 3:
                return Instant.now().plus(7, ChronoUnit.DAYS);
            case 4:
                return Instant.now().plus(14, ChronoUnit.DAYS);
            default:
                return Instant.now().plus(30, ChronoUnit.DAYS);
        }
    }

    @Transactional
    public void endSession(UUID sessionId) {
        log.info("Ending session {}", sessionId);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with ID: " + sessionId));
        session.setCompleted(true);
        session.setCompletedAt(Instant.now());
        sessionRepository.save(session);
    }

    @Cacheable("sessionSummaries")
    public String generateSessionSummary(UUID sessionId) {
        log.info("Generating summary for session {}", sessionId);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found with ID: " + sessionId));

        List<UserInteraction> interactions = userInteractionRepository.findBySessionId(sessionId);
        return chatService.generateSessionSummary(interactions);
    }

    public List<Session> getSessionsByUserId(UUID userId) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
    }
}
