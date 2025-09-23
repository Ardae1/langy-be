package com.example.languageservice.api;

import com.example.languageservice.domain.service.ChatService;
import com.example.languageservice.api.dto.ChatMessageRequest;
import com.example.languageservice.api.dto.ChatMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing AI chat interactions. This controller handles
 * the chat flow with the AI teacher and word enhancement requests. It uses the
 * ChatService to manage the chat history and delegate to the LLM.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Endpoint to send a message to the AI teacher and get a response.
     *
     * @param request The request containing the user's message and session ID.
     * @return A ResponseEntity with the AI's response, including any grounded
     * sources.
     */
    @PostMapping("/message")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        ChatMessageResponse aiResponse = chatService.sendAndReceiveMessage(
                request.getSessionId(),
                request.getMessage()
        );
        return ResponseEntity.ok(aiResponse);
    }

    /**
     * Endpoint to get an AI-generated enhanced explanation for a word.
     *
     * @param word The word to enhance.
     * @return A ResponseEntity with the enhanced word information.
     */
    @PostMapping("/enhance")
    public ResponseEntity<ChatMessageResponse> enhanceWord(@RequestBody ChatMessageRequest request) {
        // Extract word from message string for this simplified flow (e.g., "Enhance 'hello'.")
        String msg = request.getMessage();
        String word = msg;
        int s = msg.indexOf('\'');
        int e = msg.lastIndexOf('\'');
        if (s >= 0 && e > s) {
            word = msg.substring(s + 1, e);
        }
        ChatMessageResponse enhanced = chatService.enhanceWordWithAI(request.getSessionId(), word);
        return ResponseEntity.ok(enhanced);
    }
}
