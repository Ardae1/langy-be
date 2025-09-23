package com.example.languageservice.domain.service;
import com.example.languageservice.domain.model.Session;
import com.example.languageservice.domain.model.UserInteraction;
import com.example.languageservice.domain.model.UserWord;
import com.example.languageservice.domain.repository.SessionRepository;
import com.example.languageservice.domain.repository.UserInteractionRepository;
import com.example.languageservice.domain.repository.UserWordRepository;
import com.example.languageservice.domain.service.ChatService;
import com.example.languageservice.domain.service.SessionService;
import com.example.languageservice.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserInteractionRepository userInteractionRepository;

    @Mock
    private UserWordRepository userWordRepository;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private SessionService sessionService;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final String language = "en";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStartSession() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            Session session = new Session();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            when(sessionRepository.save(any(Session.class))).thenReturn(session);

            Session newSession = sessionService.startSession(language);

            assertNotNull(newSession);
            assertEquals(userId, newSession.getUserId());
            assertNotNull(newSession.getStartedAt());
            assertFalse(newSession.isCompleted());

            verify(sessionRepository, times(1)).save(any(Session.class));
        }
    }

    @Test
    public void testEndSession() {
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setCompleted(false);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        sessionService.endSession(sessionId);

        assertTrue(session.isCompleted());
        assertNotNull(session.getCompletedAt());
        verify(sessionRepository, times(1)).save(session);
    }

    @Test
    public void testGenerateSessionSummary() {
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setUserId(userId);

        UserInteraction interaction1 = new UserInteraction(UUID.randomUUID(), sessionId, "word1", "user_answer1", "correct_answer1", true);
        UserInteraction interaction2 = new UserInteraction(UUID.randomUUID(), sessionId, "word2", "user_answer2", "correct_answer2", false);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userInteractionRepository.findBySessionId(sessionId)).thenReturn(java.util.List.of(interaction1, interaction2));
        when(chatService.generateSessionSummary(anyList())).thenReturn("Summary text");

        String summary = sessionService.generateSessionSummary(sessionId);

        assertEquals("Summary text", summary);
        verify(chatService, times(1)).generateSessionSummary(anyList());
    }

    @Test
    public void testSubmitWordAnswerCorrect() {
        UserWord userWord = new UserWord();
        userWord.setUserId(userId);
        userWord.setWord("test");
        userWord.setBox(1);
        userWord.setReviewDate(Instant.now());

        when(userWordRepository.findByUserIdAndWord(userId, "test")).thenReturn(Optional.of(userWord));
        when(userWordRepository.save(any(UserWord.class))).thenAnswer(i -> i.getArguments()[0]);

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);

            sessionService.submitWordAnswer(sessionId, "test", true);

            assertEquals(2, userWord.getBox());
            assertEquals(Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.MINUTES), userWord.getReviewDate().truncatedTo(ChronoUnit.MINUTES));
            verify(userInteractionRepository, times(1)).save(any(UserInteraction.class));
            verify(userWordRepository, times(1)).save(userWord);
        }
    }
}
