package com.example.languageservice.api;

import com.example.languageservice.api.ChatController;
import com.example.languageservice.api.dto.ChatMessageRequest;
import com.example.languageservice.api.dto.ChatMessageResponse;
import com.example.languageservice.domain.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID sessionId = UUID.randomUUID();

    @Test
    public void testSendMessage() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest(sessionId, "Hello!");
        ChatMessageResponse response = new ChatMessageResponse("Hello! How can I help?", null);

        when(chatService.sendAndReceiveMessage(sessionId, "Hello!")).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hello! How can I help?"))
                .andExpect(jsonPath("$.sources").isEmpty());

        verify(chatService, times(1)).sendAndReceiveMessage(sessionId, "Hello!");
    }

    @Test
    public void testEnhanceWord() throws Exception {
        ChatMessageRequest request = new ChatMessageRequest(sessionId, "Enhance 'hello'.");
        ChatMessageResponse response = new ChatMessageResponse("Enhanced word data for 'hello'.", null);

        when(chatService.enhanceWordWithAI(sessionId, "hello")).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat/enhance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Enhanced word data for 'hello'."))
                .andExpect(jsonPath("$.sources").isEmpty());

        verify(chatService, times(1)).enhanceWordWithAI(sessionId, "hello");
    }
}
