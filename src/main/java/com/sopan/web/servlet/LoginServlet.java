package com.sopan.web.servlet;

import com.sopan.exception.AuthException;
import com.sopan.model.User;
import com.sopan.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.authService = (AuthService) getServletContext().getAttribute("authService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser != null) {
            redirect(req, resp, currentUser.homePath());
            return;
        }

        String errorParam = req.getParameter("error");
        if ("suspended".equals(errorParam)) {
            setFlashError(req, "Your account has been suspended by an administrator.");
        }

        forward(req, resp, "auth/login");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            User user = authService.login(email, password);

            // Security: change session ID on authentication to mitigate session fixation attacks
            req.changeSessionId();
            HttpSession session = req.getSession(true);
            session.setAttribute("CURRENT_USER", user);

            String redirectAfter = (String) session.getAttribute("REDIRECT_AFTER_LOGIN");
            session.removeAttribute("REDIRECT_AFTER_LOGIN");

            if (redirectAfter != null && !redirectAfter.isBlank() && !redirectAfter.contains("/login")) {
                resp.sendRedirect(redirectAfter);
            } else {
                redirect(req, resp, user.homePath());
            }

        } catch (AuthException e) {
            setFlashError(req, e.getMessage());
            req.setAttribute("email", email);
            forward(req, resp, "auth/login");
        }
    }
}
