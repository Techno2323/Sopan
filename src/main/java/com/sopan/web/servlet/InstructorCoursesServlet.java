package com.sopan.web.servlet;

import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Course;
import com.sopan.model.User;
import com.sopan.model.enums.Difficulty;
import com.sopan.service.CourseService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "InstructorCoursesServlet", urlPatterns = {"/teach/courses"})
public class InstructorCoursesServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Course> courses = courseService.getInstructorCourses(user.getId());
        req.setAttribute("courses", courses);
        forward(req, resp, "teach/courses");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                String code = req.getParameter("code");
                String title = req.getParameter("title");
                String description = req.getParameter("description");
                String category = req.getParameter("category");
                Difficulty difficulty = Difficulty.valueOf(req.getParameter("difficulty"));
                int maxStudents = Validator.parsePositiveInt(req.getParameter("maxStudents"), "Maximum students");
                int gateThreshold = Validator.parseIntOrDefault(req.getParameter("gateThreshold"), 60);

                int courseId = courseService.createCourse(user.getId(), code, title, description, category,
                        difficulty, maxStudents, gateThreshold);
                setFlashSuccess(req, "Course created! Start building your concept graph below.");
                redirect(req, resp, "/teach/course/graph?id=" + courseId);
                return;

            } else if ("submitApproval".equals(action)) {
                int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);
                courseService.submitForApproval(courseId, user.getId());
                setFlashSuccess(req, "Course submitted for administrator approval.");

            } else if ("delete".equals(action)) {
                int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);
                courseService.deleteOrArchiveCourse(courseId, user.getId());
                setFlashSuccess(req, "Course updated/archived successfully.");
            }
        } catch (ValidationException | AccessDeniedException e) {
            setFlashError(req, e.getMessage());
        }

        redirect(req, resp, "/teach/courses");
    }
}
