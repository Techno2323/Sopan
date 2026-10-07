package com.sopan.web.servlet;

import com.sopan.dto.QuizResultView;
import com.sopan.model.User;
import com.sopan.model.enums.Role;
import com.sopan.service.QuizService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "QuizResultServlet", urlPatterns = {"/learn/quiz/result"})
public class QuizResultServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private QuizService quizService;

    @Override
    public void init() throws ServletException {
        this.quizService = (QuizService) getServletContext().getAttribute("quizService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int attemptId = Validator.parseIntOrDefault(req.getParameter("attempt"), 0);

        if (attemptId <= 0) {
            redirect(req, resp, "/learn/home");
            return;
        }

        try {
            boolean isInstructor = (user.getRole() == Role.INSTRUCTOR || user.getRole() == Role.ADMIN);
            QuizResultView resultView = quizService.getAttemptResultBreakdown(attemptId, user.getId(), isInstructor);
            req.setAttribute("result", resultView);
            forward(req, resp, "learn/quiz-result");

        } catch (Exception e) {
            setFlashError(req, "Quiz result not accessible.");
            redirect(req, resp, "/learn/home");
        }
    }
}
