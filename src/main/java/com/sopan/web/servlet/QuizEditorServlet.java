package com.sopan.web.servlet;

import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Concept;
import com.sopan.model.Course;
import com.sopan.model.Option;
import com.sopan.model.Question;
import com.sopan.model.Quiz;
import com.sopan.model.User;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.service.QuizService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "QuizEditorServlet", urlPatterns = {"/teach/quiz/edit"})
public class QuizEditorServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private QuizService quizService;
    private CourseService courseService;
    private ConceptService conceptService;

    @Override
    public void init() throws ServletException {
        this.quizService = (QuizService) getServletContext().getAttribute("quizService");
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int quizId = Validator.parseIntOrDefault(req.getParameter("id"), 0);

        if (quizId <= 0) {
            redirect(req, resp, "/teach/quizzes");
            return;
        }

        try {
            Quiz quiz = quizService.getQuiz(quizId);
            Course course = courseService.getCourse(quiz.getCourseId());
            courseService.checkInstructorOwnership(course, user.getId());

            List<Concept> concepts = conceptService.getCourseConcepts(course.getCourseId());
            List<Question> questions = quizService.getQuizQuestions(quizId);

            Map<Integer, List<Option>> optionsMap = new HashMap<>();
            for (Question q : questions) {
                optionsMap.put(q.getQuestionId(), quizService.getQuestionOptions(q.getQuestionId()));
            }

            req.setAttribute("quiz", quiz);
            req.setAttribute("course", course);
            req.setAttribute("concepts", concepts);
            req.setAttribute("questions", questions);
            req.setAttribute("optionsMap", optionsMap);

            forward(req, resp, "teach/quiz-editor");

        } catch (AccessDeniedException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/teach/quizzes");
        } catch (Exception e) {
            setFlashError(req, "Quiz not found.");
            redirect(req, resp, "/teach/quizzes");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int quizId = Validator.parseIntOrDefault(req.getParameter("quizId"), 0);
        String action = req.getParameter("action");

        if (quizId <= 0) {
            redirect(req, resp, "/teach/quizzes");
            return;
        }

        try {
            if ("addQuestion".equals(action)) {
                int conceptId = Validator.parseIntOrDefault(req.getParameter("conceptId"), 0);
                String prompt = req.getParameter("prompt");
                int marks = Validator.parseIntOrDefault(req.getParameter("marks"), 1);

                String[] optionLabels = req.getParameterValues("optionLabel");
                int correctIdx = Validator.parseIntOrDefault(req.getParameter("correctOptionIndex"), 0);

                if (optionLabels == null || optionLabels.length < 2) {
                    throw new ValidationException("Please provide at least two choices.");
                }

                List<Option> options = new ArrayList<>();
                for (int i = 0; i < optionLabels.length; i++) {
                    String lbl = optionLabels[i];
                    if (lbl != null && !lbl.isBlank()) {
                        Option opt = new Option();
                        opt.setLabel(lbl.trim());
                        opt.setCorrect(i == correctIdx);
                        options.add(opt);
                    }
                }

                quizService.addQuestion(quizId, user.getId(), conceptId, prompt, marks, options);
                setFlashSuccess(req, "Question added and tagged to concept.");

            } else if ("publish".equals(action)) {
                quizService.publishQuiz(quizId, user.getId());
                setFlashSuccess(req, "Quiz successfully published to students!");
            }
        } catch (ValidationException | AccessDeniedException e) {
            setFlashError(req, e.getMessage());
        }

        redirect(req, resp, "/teach/quiz/edit?id=" + quizId);
    }
}
