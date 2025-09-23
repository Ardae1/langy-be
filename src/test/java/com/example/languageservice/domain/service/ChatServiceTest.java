package com.example.languageservice.domain.service;
import com.example.languageservice.domain.model.ChatMessage;
import com.example.languageservice.domain.model.Session;
import com.example.languageservice.domain.repository.ChatMessageRepository;
import com.example.languageservice.domain.repository.SessionRepository;
import com.example.languageservice.domain.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChatServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private ChatService chatService;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private Session session;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new Session();
        session.setSessionId(sessionId);
        session.setUserId(userId);
    }

    @Test
    public void testSendAndReceiveMessage() throws IOException, InterruptedException {
        // Mock repository calls
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId))
                .thenReturn(Collections.emptyList());
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mock HTTP client response
        String aiResponseJson = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello! How can I help?\"}]}}]}";
        when(httpResponse.body()).thenReturn(aiResponseJson);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Call the service method
        String userMessage = "Hi";
        String response = chatService.sendAndReceiveMessage(sessionId, userMessage).getResponse();

        // Verify interactions and result
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
        assertEquals("Hello! How can I help?", response);
    }

    @Test
    public void testEnhanceWordWithAI() throws IOException, InterruptedException {
        // Mock repository calls
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Mock HTTP client response
        String enhancedWordJson = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Enhanced word data\"}]}}]}";
        when(httpResponse.body()).thenReturn(enhancedWordJson);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

        // Call the service method
        String word = "hello";
        String response = chatService.enhanceWordWithAI(sessionId, word).getResponse();

        // Verify the result
        assertEquals("Enhanced word data", response);
    }
}
