package com.sopan.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfTokens {

    public static final String SESSION_ATTR = "CSRF_TOKEN";
    public static final String PARAMETER_NAME = "csrfToken";
    public static final String HEADER_NAME = "X-CSRF-Token";

    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokens() {}

    /**
     * Retrieves existing CSRF token from session or generates and stores a new one.
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return "";
        }
        String token = (String) session.getAttribute(SESSION_ATTR);
        if (token == null || token.isBlank()) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_ATTR, token);
        }
        return token;
    }

    /**
     * Validates incoming request token against session token using constant-time comparison.
     */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(SESSION_ATTR);
        if (sessionToken == null || sessionToken.isBlank()) {
            return false;
        }

        String requestToken = request.getParameter(PARAMETER_NAME);
        if (requestToken == null || requestToken.isBlank()) {
            requestToken = request.getHeader(HEADER_NAME);
        }

        if (requestToken == null || requestToken.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                sessionToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                requestToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
