package com.sopan.web.servlet;

import com.sopan.dto.CohortRow;
import com.sopan.model.Course;
import com.sopan.model.Submission;
import com.sopan.model.User;
import com.sopan.service.AssignmentService;
import com.sopan.service.CourseService;
import com.sopan.service.InterventionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "InstructorHomeServlet", urlPatterns = {"/teach/home"})
public class InstructorHomeServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private AssignmentService assignmentService;
    private InterventionService interventionService;

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.assignmentService = (AssignmentService) getServletContext().getAttribute("assignmentService");
        this.interventionService = (InterventionService) getServletContext().getAttribute("interventionService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Course> courses = courseService.getInstructorCourses(user.getId());
        List<Submission> pendingGrading = assignmentService.getPendingGrading(user.getId());

        int totalStalled = 0;
        for (Course c : courses) {
            try {
                List<CohortRow> cohort = interventionService.getCohortHeatmap(c.getCourseId(), user.getId());
                for (CohortRow row : cohort) {
                    if (row.isStalled()) totalStalled++;
                }
            } catch (Exception ignored) {}
        }

        req.setAttribute("courses", courses);
        req.setAttribute("pendingGrading", pendingGrading);
        req.setAttribute("totalStalled", totalStalled);

        forward(req, resp, "teach/home");
    }
}
