package com.sopan.web.servlet;

import com.sopan.exception.DuplicateAttemptException;
import com.sopan.exception.PrerequisiteNotMetException;
import com.sopan.exception.SopanException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Option;
import com.sopan.model.Question;
import com.sopan.model.Quiz;
import com.sopan.model.User;
import com.sopan.service.EnrollmentService;
import com.sopan.service.MasteryService;
import com.sopan.service.QuizService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "TakeQuizServlet", urlPatterns = {"/learn/quiz"})
public class TakeQuizServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private QuizService quizService;
    private MasteryService masteryService;
    private EnrollmentService enrollmentService;

    @Override
    public void init() throws ServletException {
        this.quizService = (QuizService) getServletContext().getAttribute("quizService");
        this.masteryService = (MasteryService) getServletContext().getAttribute("masteryService");
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int quizId = Validator.parseIntOrDefault(req.getParameter("id"), 0);

        if (quizId <= 0) {
            redirect(req, resp, "/learn/home");
            return;
        }

        try {
            Quiz quiz = quizService.getQuiz(quizId);

            if (!enrollmentService.isStudentEnrolled(quiz.getCourseId(), user.getId())) {
                setFlashError(req, "You must be enrolled in this course to take this quiz.");
                redirect(req, resp, "/catalog/course?id=" + quiz.getCourseId());
                return;
            }

            List<Question> questions = quizService.getQuizQuestions(quizId);
            Map<Integer, List<Option>> optionsMap = new LinkedHashMap<>();

            // Check gating for all concepts covered in the quiz
            for (Question q : questions) {
                masteryService.checkPrerequisitesMet(user.getId(), quiz.getCourseId(), q.getConceptId());
                optionsMap.put(q.getQuestionId(), quizService.getQuestionOptions(q.getQuestionId()));
            }

            req.setAttribute("quiz", quiz);
            req.setAttribute("questions", questions);
            req.setAttribute("optionsMap", optionsMap);

            forward(req, resp, "learn/take-quiz");

        } catch (PrerequisiteNotMetException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/learn/home");
        } catch (Exception e) {
            setFlashError(req, "Quiz not found.");
            redirect(req, resp, "/learn/home");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int quizId = Validator.parseIntOrDefault(req.getParameter("quizId"), 0);

        if (quizId <= 0) {
            redirect(req, resp, "/learn/home");
            return;
        }

        Map<Integer, Integer> answers = new HashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            if (name.startsWith("q_")) {
                int questionId = Integer.parseInt(name.substring(2));
                int optionId = Integer.parseInt(req.getParameter(name));
                answers.put(questionId, optionId);
            }
        }

        try {
            int attemptId = quizService.submitQuizAttempt(user.getId(), quizId, answers);
            setFlashSuccess(req, "Quiz submitted successfully!");
            redirect(req, resp, "/learn/quiz/result?attempt=" + attemptId);

        } catch (DuplicateAttemptException e) {
            setFlashSuccess(req, "Your quiz submission was already received.");
            redirect(req, resp, "/learn/quiz/result?attempt=" + e.getExistingAttemptId());

        } catch (PrerequisiteNotMetException | ValidationException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/learn/quiz?id=" + quizId);

        } catch (SopanException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/learn/quiz?id=" + quizId);
        }
    }
}
