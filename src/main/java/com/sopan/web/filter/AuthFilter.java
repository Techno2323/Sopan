package com.sopan.web.filter;

import com.sopan.model.User;
import com.sopan.model.enums.AccountStatus;
import com.sopan.service.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/learn/*", "/teach/*", "/admin/*"})
public class AuthFilter implements Filter {

    private AuthService authService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext context = filterConfig.getServletContext();
        this.authService = (AuthService) context.getAttribute("authService");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("CURRENT_USER") : null;

        if (currentUser == null) {
            String redirectUrl = httpRequest.getRequestURI();
            if (httpRequest.getQueryString() != null) {
                redirectUrl += "?" + httpRequest.getQueryString();
            }
            httpRequest.getSession(true).setAttribute("REDIRECT_AFTER_LOGIN", redirectUrl);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        // Active status verification: suspended users are logged out immediately
        try {
            if (authService != null) {
                User freshUser = authService.verifyActiveSessionUser(currentUser.getId());
                session.setAttribute("CURRENT_USER", freshUser);
                currentUser = freshUser;
            }
        } catch (Exception e) {
            session.invalidate();
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?error=suspended");
            return;
        }

        if (currentUser.getStatus() == AccountStatus.SUSPENDED) {
            session.invalidate();
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?error=suspended");
            return;
        }

        chain.doFilter(request, response);
    }
}
