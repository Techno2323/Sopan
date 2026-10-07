package com.sopan.web.servlet;

import com.sopan.exception.AuthException;
import com.sopan.exception.ValidationException;
import com.sopan.model.User;
import com.sopan.service.AuthService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends BaseServlet {
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
        forward(req, resp, "auth/register");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String roleStr = req.getParameter("role");
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            User newUser;
            if ("INSTRUCTOR".equalsIgnoreCase(roleStr)) {
                String department = req.getParameter("department");
                String bio = req.getParameter("bio");
                newUser = authService.registerInstructor(fullName, email, password, department, bio);
            } else {
                String rollNo = req.getParameter("rollNo");
                String program = req.getParameter("program");
                int studyYear = Validator.parseIntOrDefault(req.getParameter("studyYear"), 1);
                newUser = authService.registerStudent(fullName, email, password, rollNo, program, studyYear);
            }

            req.changeSessionId();
            req.getSession(true).setAttribute("CURRENT_USER", newUser);
            setFlashSuccess(req, "Account created successfully! Welcome to Sopan LMS.");
            redirect(req, resp, newUser.homePath());

        } catch (ValidationException | AuthException e) {
            setFlashError(req, e.getMessage());
            req.setAttribute("role", roleStr);
            req.setAttribute("fullName", fullName);
            req.setAttribute("email", email);
            forward(req, resp, "auth/register");
        }
    }
}
