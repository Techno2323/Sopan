package com.sopan.web.servlet;

import com.sopan.model.User;
import com.sopan.security.CsrfTokens;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Base abstract servlet providing:
 * - Current user extraction
 * - Flash messaging for Post-Redirect-Get patterns
 * - Forwarding to /WEB-INF/views/*.jsp
 * - Redirect helpers
 * - Clean handleGet and handlePost template methods
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String FLASH_SUCCESS = "FLASH_SUCCESS";
    public static final String FLASH_ERROR = "FLASH_ERROR";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        extractFlashMessages(req);
        handleGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handlePost(req, resp);
    }

    protected abstract void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException;

    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Default implementation if servlet only handles GET
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    protected User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null ? (User) session.getAttribute("CURRENT_USER") : null;
    }

    protected void setFlashSuccess(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(FLASH_SUCCESS, message);
    }

    protected void setFlashError(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(FLASH_ERROR, message);
    }

    private void extractFlashMessages(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String success = (String) session.getAttribute(FLASH_SUCCESS);
            if (success != null) {
                req.setAttribute("flashSuccess", success);
                session.removeAttribute(FLASH_SUCCESS);
            }
            String error = (String) session.getAttribute(FLASH_ERROR);
            if (error != null) {
                req.setAttribute("flashError", error);
                session.removeAttribute(FLASH_ERROR);
            }
        }
    }

    /**
     * Forwards request to a JSP view under /WEB-INF/views/
     *
     * @param viewPath path relative to /WEB-INF/views/ (e.g. "learn/home" or "learn/home.jsp")
     */
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String viewPath)
            throws ServletException, IOException {
        String fullPath = "/WEB-INF/views/" + (viewPath.endsWith(".jsp") ? viewPath : viewPath + ".jsp");
        // Ensure CSRF token is available
        String token = CsrfTokens.getToken(req.getSession(true));
        req.setAttribute("csrfToken", token);
        req.getRequestDispatcher(fullPath).forward(req, resp);
    }

    /**
     * Redirects using context-relative path (e.g. "/learn/home")
     */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        String contextPath = req.getContextPath();
        String target = path.startsWith("/") ? contextPath + path : contextPath + "/" + path;
        resp.sendRedirect(target);
    }
}
