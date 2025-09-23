package com.example.languageservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

/**
 * Utility class to access security context information. This class provides a
 * centralized way to retrieve the authenticated user's ID, which is a best
 * practice to avoid passing sensitive information in the request body or URL.
 */
public class SecurityUtils {

    /**
     * Retrieves the UUID of the currently authenticated user. The UUID is
     * assumed to be the 'sub' claim from the JWT, which is stored as the
     * principal name in the SecurityContext.
     *
     * @return The UUID of the authenticated user.
     * @throws IllegalStateException if no user is authenticated.
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }
}
