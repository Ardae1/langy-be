package com.example.languageservice.api;

import com.example.languageservice.domain.service.SessionService;
import com.example.languageservice.domain.model.Session;
import com.example.languageservice.api.dto.StartSessionRequest;
import com.example.languageservice.api.dto.AnswerRequest;
import com.example.languageservice.api.dto.EndSessionRequest;
import com.example.languageservice.api.dto.SessionSummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing learning sessions. This class handles all
 * incoming HTTP requests related to sessions. It's part of the API layer and
 * delegates business logic to the SessionService. This controller is designed
 * with a standard, blocking model, leveraging Java 21's Virtual Threads for
 * high concurrency without the complexity of reactive programming.
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Endpoint to start a new learning session.
     *
     * @param request The request body containing user, language, and session
     * type.
     * @return A ResponseEntity with the newly created Session.
     */
    @PostMapping("/start")
    public ResponseEntity<Session> startSession(@RequestBody StartSessionRequest request) {
        Session session = sessionService.startSession(request.getLanguage());
        return ResponseEntity.ok(session);
    }

    /**
     * Endpoint to submit a word answer for a session.
     *
     * @param sessionId The ID of the session.
     * @param request The request body containing the word ID and correctness.
     * @return A ResponseEntity indicating success.
     */
    @PostMapping("/answer")
    public ResponseEntity<Void> submitAnswer(@RequestBody AnswerRequest request) {
        sessionService.submitWordAnswer(request.getSessionId(), request.getWord(), request.isCorrect());
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to end an ongoing session.
     *
     * @param request The request body containing the session ID.
     * @return A ResponseEntity indicating success.
     */
    @PostMapping("/end")
    public ResponseEntity<Void> endSession(@RequestBody EndSessionRequest request) {
        sessionService.endSession(request.getSessionId());
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to retrieve a user's historical sessions.
     *
     * @param userId The ID of the user.
     * @return A list of sessions for the user.
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<Session>> getSessionHistory(@PathVariable UUID userId) {
        List<Session> sessions = sessionService.getSessionsByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Endpoint to get an AI-generated summary of a completed session.
     *
     * @param sessionId The ID of the session.
     * @return A ResponseEntity with the session summary.
     */
    @GetMapping("/{sessionId}/summary")
    public ResponseEntity<SessionSummaryResponse> getSessionSummary(@PathVariable UUID sessionId) {
        String summaryText = sessionService.generateSessionSummary(sessionId);
        // Construct a minimal response; if you need timestamps, fetch session and set fields
        SessionSummaryResponse resp = new SessionSummaryResponse(sessionId, summaryText, null, null);
        return ResponseEntity.ok(resp);
    }
}
