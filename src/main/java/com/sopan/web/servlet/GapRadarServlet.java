package com.sopan.web.servlet;

import com.sopan.dto.RootCauseView;
import com.sopan.model.Course;
import com.sopan.model.Enrollment;
import com.sopan.model.User;
import com.sopan.service.CourseService;
import com.sopan.service.EnrollmentService;
import com.sopan.service.RecommendationService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "GapRadarServlet", urlPatterns = {"/learn/gaps"})
public class GapRadarServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private EnrollmentService enrollmentService;
    private CourseService courseService;
    private RecommendationService recommendationService;

    @Override
    public void init() throws ServletException {
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.recommendationService = (RecommendationService) getServletContext().getAttribute("recommendationService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(user.getId());

        List<Course> enrolledCourses = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if (e.getStatus() == Enrollment.Status.ACTIVE) {
                enrolledCourses.add(courseService.getCourse(e.getCourseId()));
            }
        }

        int selectedCourseId = Validator.parseIntOrDefault(req.getParameter("course"), 0);
        if (selectedCourseId <= 0 && !enrolledCourses.isEmpty()) {
            selectedCourseId = enrolledCourses.get(0).getCourseId();
        }

        List<RootCauseView> rootCauses = new ArrayList<>();
        Course selectedCourse = null;

        if (selectedCourseId > 0) {
            final int cId = selectedCourseId;
            selectedCourse = enrolledCourses.stream().filter(c -> c.getCourseId() == cId).findFirst().orElse(null);
            if (selectedCourse != null) {
                rootCauses = recommendationService.getGapRadar(user.getId(), selectedCourseId);
            }
        }

        req.setAttribute("enrolledCourses", enrolledCourses);
        req.setAttribute("selectedCourse", selectedCourse);
        req.setAttribute("rootCauses", rootCauses);

        forward(req, resp, "learn/gaps");
    }
}
