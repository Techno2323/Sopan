package com.sopan.web.servlet;

import com.sopan.engine.ConceptGraph;
import com.sopan.engine.GraphLayout;
import com.sopan.model.Course;
import com.sopan.model.User;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.service.EnrollmentService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "CoursePreviewServlet", urlPatterns = {"/catalog/course"})
public class CoursePreviewServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private ConceptService conceptService;
    private EnrollmentService enrollmentService;
    private final GraphLayout graphLayout = new GraphLayout();

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int courseId = Validator.parseIntOrDefault(req.getParameter("id"), 0);
        if (courseId <= 0) {
            redirect(req, resp, "/catalog");
            return;
        }

        try {
            Course course = courseService.getCourse(courseId);
            ConceptGraph graph = conceptService.getConceptGraph(courseId);
            GraphLayout.LayoutResult layoutResult = graphLayout.layout(graph, null);

            req.setAttribute("course", course);
            req.setAttribute("graph", graph);
            req.setAttribute("layoutResult", layoutResult);

            User user = getCurrentUser(req);
            boolean enrolled = false;
            if (user != null) {
                enrolled = enrollmentService.isStudentEnrolled(courseId, user.getId());
            }
            req.setAttribute("isEnrolled", enrolled);

            forward(req, resp, "catalog/preview");
        } catch (Exception e) {
            setFlashError(req, "Course not found.");
            redirect(req, resp, "/catalog");
        }
    }
}
