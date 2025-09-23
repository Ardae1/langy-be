package com.example.languageservice.interceptor;

import com.example.languageservice.domain.model.UserActivity;
import com.example.languageservice.domain.repository.UserActivityRepository;
import com.example.languageservice.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNullApi;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;

@Component
public class UserActivityInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserActivityInterceptor.class);

    private final UserActivityRepository userActivityRepository;

    public UserActivityInterceptor(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // Extract session ID and user ID
            String sessionIdHeader = request.getHeader("X-Session-ID");
            if (sessionIdHeader == null) {
                log.warn("No session ID provided in the request headers.");
                return true; // Continue processing the request
            }

            UUID sessionId = UUID.fromString(sessionIdHeader);
            UUID userId = SecurityUtils.getCurrentUserId();

            // Determine action type based on the request path
            String actionType = determineActionType(request.getRequestURI());
            if (actionType == null) {
                return true; // Skip logging for unrelated paths
            }

            // Log the activity
            UserActivity activity = new UserActivity();
            activity.setId(UUID.randomUUID());
            activity.setSessionId(sessionId);
            activity.setUserId(userId);
            activity.setActionType(actionType);
            activity.setActionDetails(request.getRequestURI());
            activity.setTimestamp(Instant.now());

            userActivityRepository.save(activity);
            log.info("Logged user activity: {} for session {}", actionType, sessionId);
        } catch (Exception e) {
            log.error("Failed to log user activity", e);
        }
        return true; // Continue processing the request
    }

    private String determineActionType(String requestUri) {
        if (requestUri.contains("/sessions/start")) {
            return "SESSION_STARTED";
        } else if (requestUri.contains("/sessions/end")) {
            return "SESSION_ENDED";
        } else if (requestUri.contains("/chat/message")) {
            return "CHAT_MESSAGE";
        } else if (requestUri.contains("/chat/enhance")) {
            return "WORD_ENHANCED";
        } else if (requestUri.contains("/sessions/answer")) {
            return "WORD_ANSWERED";
        }
        return null;
    }
}