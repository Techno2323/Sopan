package com.sopan.web.servlet;

import com.sopan.model.Course;
import com.sopan.model.Enrollment;
import com.sopan.model.User;
import com.sopan.service.CourseService;
import com.sopan.service.EnrollmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "StudentCoursesServlet", urlPatterns = {"/learn/courses"})
public class StudentCoursesServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private EnrollmentService enrollmentService;
    private CourseService courseService;

    @Override
    public void init() throws ServletException {
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(user.getId());

        List<Course> activeCourses = new ArrayList<>();
        List<Course> droppedCourses = new ArrayList<>();

        for (Enrollment e : enrollments) {
            Course c = courseService.getCourse(e.getCourseId());
            if (e.getStatus() == Enrollment.Status.ACTIVE) {
                activeCourses.add(c);
            } else if (e.getStatus() == Enrollment.Status.DROPPED) {
                droppedCourses.add(c);
            }
        }

        req.setAttribute("activeCourses", activeCourses);
        req.setAttribute("droppedCourses", droppedCourses);
        forward(req, resp, "learn/courses");
    }
}
