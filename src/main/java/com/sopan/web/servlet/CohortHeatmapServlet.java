package com.sopan.web.servlet;

import com.sopan.dto.CohortRow;
import com.sopan.model.Concept;
import com.sopan.model.Course;
import com.sopan.model.User;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.service.InterventionService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CohortHeatmapServlet", urlPatterns = {"/teach/cohort"})
public class CohortHeatmapServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private ConceptService conceptService;
    private InterventionService interventionService;

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
        this.interventionService = (InterventionService) getServletContext().getAttribute("interventionService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Course> courses = courseService.getInstructorCourses(user.getId());

        int selectedCourseId = Validator.parseIntOrDefault(req.getParameter("course"), 0);
        if (selectedCourseId <= 0 && !courses.isEmpty()) {
            selectedCourseId = courses.get(0).getCourseId();
        }

        Course selectedCourse = null;
        List<Concept> concepts = new ArrayList<>();
        List<CohortRow> cohortRows = new ArrayList<>();

        if (selectedCourseId > 0) {
            final int cId = selectedCourseId;
            selectedCourse = courses.stream().filter(c -> c.getCourseId() == cId).findFirst().orElse(null);
            if (selectedCourse != null) {
                concepts = conceptService.getCourseConcepts(selectedCourseId);
                try {
                    cohortRows = interventionService.getCohortHeatmap(selectedCourseId, user.getId());
                } catch (Exception e) {
                    setFlashError(req, "Error computing cohort heatmap: " + e.getMessage());
                }
            }
        }

        req.setAttribute("courses", courses);
        req.setAttribute("selectedCourse", selectedCourse);
        req.setAttribute("concepts", concepts);
        req.setAttribute("cohortRows", cohortRows);

        forward(req, resp, "teach/cohort-heatmap");
    }
}
