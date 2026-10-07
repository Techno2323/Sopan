package com.sopan.web.filter;

import com.sopan.model.User;
import com.sopan.model.enums.Role;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "RoleFilter", urlPatterns = {"/learn/*", "/teach/*", "/admin/*"})
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("CURRENT_USER") : null;

        if (currentUser == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        Role role = currentUser.getRole();

        boolean allowed = false;
        if (role == Role.ADMIN) {
            allowed = true; // Admin has full system access
        } else if (path.startsWith("/learn") && role == Role.STUDENT) {
            allowed = true;
        } else if (path.startsWith("/teach") && role == Role.INSTRUCTOR) {
            allowed = true;
        }

        if (!allowed) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this resource.");
            return;
        }

        chain.doFilter(request, response);
    }
}
