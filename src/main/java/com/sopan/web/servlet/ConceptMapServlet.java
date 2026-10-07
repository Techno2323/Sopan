package com.sopan.web.servlet;

import com.sopan.dto.ConceptStatusView;
import com.sopan.engine.ConceptGraph;
import com.sopan.engine.GraphLayout;
import com.sopan.model.Course;
import com.sopan.model.User;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.service.EnrollmentService;
import com.sopan.service.MasteryService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "ConceptMapServlet", urlPatterns = {"/learn/course/map"})
public class ConceptMapServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private ConceptService conceptService;
    private MasteryService masteryService;
    private EnrollmentService enrollmentService;
    private final GraphLayout graphLayout = new GraphLayout();

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
        this.masteryService = (MasteryService) getServletContext().getAttribute("masteryService");
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int courseId = Validator.parseIntOrDefault(req.getParameter("id"), 0);

        if (courseId <= 0) {
            redirect(req, resp, "/learn/courses");
            return;
        }

        if (!enrollmentService.isStudentEnrolled(courseId, user.getId())) {
            setFlashError(req, "You must be enrolled to view this concept map.");
            redirect(req, resp, "/catalog/course?id=" + courseId);
            return;
        }

        Course course = courseService.getCourse(courseId);
        ConceptGraph graph = conceptService.getConceptGraph(courseId);
        Map<Integer, ConceptStatusView> statuses = masteryService.getAllConceptStatuses(user.getId(), courseId);
        GraphLayout.LayoutResult layoutResult = graphLayout.layout(graph, statuses);

        req.setAttribute("course", course);
        req.setAttribute("graph", graph);
        req.setAttribute("statuses", statuses);
        req.setAttribute("layoutResult", layoutResult);

        forward(req, resp, "learn/concept-map");
    }
}
