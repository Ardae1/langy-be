package com.example.languageservice.api;

import com.example.languageservice.domain.model.Session;
import com.example.languageservice.domain.service.SessionService;
import com.example.languageservice.api.SessionController;
import com.example.languageservice.api.dto.StartSessionRequest;
import com.example.languageservice.api.dto.AnswerRequest;
import com.example.languageservice.api.dto.EndSessionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID sessionId = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        Session session = new Session();
        session.setSessionId(sessionId);
        when(sessionService.startSession(anyString())).thenReturn(session);
    }

    @Test
    public void testStartSession() throws Exception {
        StartSessionRequest request = new StartSessionRequest("en");

        mockMvc.perform(post("/api/v1/sessions/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()));

        verify(sessionService, times(1)).startSession("en");
    }

    @Test
    public void testSubmitWordAnswer() throws Exception {
        AnswerRequest request = new AnswerRequest(sessionId, "hello", true);

        mockMvc.perform(post("/api/v1/sessions/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).submitWordAnswer(sessionId, "hello", true);
    }

    @Test
    public void testEndSession() throws Exception {
        EndSessionRequest request = new EndSessionRequest(sessionId);

        mockMvc.perform(post("/api/v1/sessions/end")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).endSession(sessionId);
    }

    @Test
    public void testGetSessionSummary() throws Exception {
        String summary = "This is a summary of the session.";
        when(sessionService.generateSessionSummary(sessionId)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/sessions/{sessionId}/summary", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(summary));

        verify(sessionService, times(1)).generateSessionSummary(sessionId);
    }
}
