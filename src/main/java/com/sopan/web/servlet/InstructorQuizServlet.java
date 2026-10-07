package com.sopan.web.servlet;

import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Course;
import com.sopan.model.Quiz;
import com.sopan.model.User;
import com.sopan.service.CourseService;
import com.sopan.service.QuizService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "InstructorQuizServlet", urlPatterns = {"/teach/quizzes"})
public class InstructorQuizServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private QuizService quizService;

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.quizService = (QuizService) getServletContext().getAttribute("quizService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Course> courses = courseService.getInstructorCourses(user.getId());

        int selectedCourseId = Validator.parseIntOrDefault(req.getParameter("course"), 0);
        if (selectedCourseId <= 0 && !courses.isEmpty()) {
            selectedCourseId = courses.get(0).getCourseId();
        }

        List<Quiz> quizzes = new ArrayList<>();
        Course selectedCourse = null;

        if (selectedCourseId > 0) {
            final int cId = selectedCourseId;
            selectedCourse = courses.stream().filter(c -> c.getCourseId() == cId).findFirst().orElse(null);
            if (selectedCourse != null) {
                quizzes = quizService.getCourseQuizzes(selectedCourseId);
            }
        }

        req.setAttribute("courses", courses);
        req.setAttribute("selectedCourse", selectedCourse);
        req.setAttribute("quizzes", quizzes);

        forward(req, resp, "teach/quizzes");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        String action = req.getParameter("action");
        int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);

        try {
            if ("create".equals(action)) {
                String title = req.getParameter("title");
                int maxAttempts = Validator.parseIntOrDefault(req.getParameter("maxAttempts"), 3);

                int quizId = quizService.createQuiz(courseId, user.getId(), title, maxAttempts);
                setFlashSuccess(req, "Quiz created. Add concept-tagged questions below.");
                redirect(req, resp, "/teach/quiz/edit?id=" + quizId);
                return;
            }
        } catch (ValidationException | AccessDeniedException e) {
            setFlashError(req, e.getMessage());
        }

        redirect(req, resp, "/teach/quizzes?course=" + courseId);
    }
}
