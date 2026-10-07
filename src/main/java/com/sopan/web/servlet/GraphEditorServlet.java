package com.sopan.web.servlet;

import com.sopan.engine.ConceptGraph;
import com.sopan.engine.GraphLayout;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.CyclicDependencyException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Concept;
import com.sopan.model.Course;
import com.sopan.model.User;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "GraphEditorServlet", urlPatterns = {"/teach/course/graph"})
public class GraphEditorServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private ConceptService conceptService;
    private final GraphLayout graphLayout = new GraphLayout();

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int courseId = Validator.parseIntOrDefault(req.getParameter("id"), 0);

        if (courseId <= 0) {
            redirect(req, resp, "/teach/courses");
            return;
        }

        try {
            Course course = courseService.getCourse(courseId);
            courseService.checkInstructorOwnership(course, user.getId());

            List<Concept> concepts = conceptService.getCourseConcepts(courseId);
            ConceptGraph graph = conceptService.getConceptGraph(courseId);
            GraphLayout.LayoutResult layoutResult = graphLayout.layout(graph, null);

            req.setAttribute("course", course);
            req.setAttribute("concepts", concepts);
            req.setAttribute("graph", graph);
            req.setAttribute("layoutResult", layoutResult);

            forward(req, resp, "teach/graph-editor");

        } catch (AccessDeniedException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/teach/courses");
        } catch (Exception e) {
            setFlashError(req, "Error loading concept graph: " + e.getMessage());
            redirect(req, resp, "/teach/courses");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);
        String action = req.getParameter("action");

        if (courseId <= 0) {
            redirect(req, resp, "/teach/courses");
            return;
        }

        try {
            if ("addConcept".equals(action)) {
                String title = req.getParameter("title");
                String summary = req.getParameter("summary");
                int displayOrder = Validator.parseIntOrDefault(req.getParameter("displayOrder"), 0);

                conceptService.createConcept(courseId, user.getId(), title, summary, displayOrder);
                setFlashSuccess(req, "Concept added successfully.");

            } else if ("deleteConcept".equals(action)) {
                int conceptId = Validator.parseIntOrDefault(req.getParameter("conceptId"), 0);
                conceptService.deleteConcept(conceptId, user.getId());
                setFlashSuccess(req, "Concept deleted successfully.");

            } else if ("savePrerequisites".equals(action)) {
                List<Concept> concepts = conceptService.getCourseConcepts(courseId);
                Map<Integer, Set<Integer>> proposed = new HashMap<>();

                for (Concept c : concepts) {
                    proposed.put(c.getConceptId(), new HashSet<>());
                    String[] prereqParams = req.getParameterValues("prereq_" + c.getConceptId());
                    if (prereqParams != null) {
                        for (String pStr : prereqParams) {
                            int pId = Integer.parseInt(pStr);
                            if (pId != c.getConceptId()) {
                                proposed.get(c.getConceptId()).add(pId);
                            }
                        }
                    }
                }

                conceptService.savePrerequisites(courseId, user.getId(), proposed);
                setFlashSuccess(req, "Prerequisite graph saved and validated successfully.");
            }
        } catch (CyclicDependencyException e) {
            setFlashError(req, "Cycle detected! Saving was refused because the proposed prerequisites create a circular dependency: " + e.getCyclePath());
        } catch (ValidationException | AccessDeniedException e) {
            setFlashError(req, e.getMessage());
        }

        redirect(req, resp, "/teach/course/graph?id=" + courseId);
    }
}
