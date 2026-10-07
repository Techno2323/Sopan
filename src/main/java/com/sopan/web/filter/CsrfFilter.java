package com.sopan.web.filter;

import com.sopan.security.CsrfTokens;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

@WebFilter(filterName = "CsrfFilter", urlPatterns = "/*")
public class CsrfFilter implements Filter {

    private static final Set<String> METHODS_TO_CHECK = Set.of("POST", "PUT", "DELETE", "PATCH");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Ensure token exists on session for EL expression ${csrfToken}
        String token = CsrfTokens.getToken(httpRequest.getSession(true));
        httpRequest.setAttribute("csrfToken", token);

        String method = httpRequest.getMethod().toUpperCase();
        if (METHODS_TO_CHECK.contains(method)) {
            if (!CsrfTokens.isValid(httpRequest)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
