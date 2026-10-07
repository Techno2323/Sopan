package com.sopan.web.servlet;

import com.sopan.dao.AssignmentDao;
import com.sopan.dao.MaterialDao;
import com.sopan.dao.QuizDao;
import com.sopan.dao.MasterySnapshotDao;
import com.sopan.dto.ConceptStatusView;
import com.sopan.engine.ConceptGraph;
import com.sopan.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet(name = "ConceptDetailServlet", urlPatterns = {"/learn/concept"})
public class ConceptDetailServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private ConceptService conceptService;
    private CourseService courseService;
    private MasteryService masteryService;
    private EnrollmentService enrollmentService;
    private MaterialDao materialDao;
    private QuizDao quizDao;
    private AssignmentDao assignmentDao;
    private MasterySnapshotDao masterySnapshotDao;

    @Override
    public void init() throws ServletException {
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.masteryService = (MasteryService) getServletContext().getAttribute("masteryService");
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
        this.materialDao = (MaterialDao) getServletContext().getAttribute("materialDao");
        this.quizDao = (QuizDao) getServletContext().getAttribute("quizDao");
        this.assignmentDao = (AssignmentDao) getServletContext().getAttribute("assignmentDao");
        this.masterySnapshotDao = (MasterySnapshotDao) getServletContext().getAttribute("masterySnapshotDao");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int conceptId = Validator.parseIntOrDefault(req.getParameter("id"), 0);

        if (conceptId <= 0) {
            redirect(req, resp, "/learn/home");
            return;
        }

        try {
            Concept concept = conceptService.getConcept(conceptId);
            Course course = courseService.getCourse(concept.getCourseId());

            if (!enrollmentService.isStudentEnrolled(course.getCourseId(), user.getId())) {
                setFlashError(req, "You must be enrolled in this course to view concept details.");
                redirect(req, resp, "/catalog/course?id=" + course.getCourseId());
                return;
            }

            ConceptStatusView statusView = masteryService.getConceptStatus(user.getId(), course.getCourseId(), conceptId);
            List<Material> materials = materialDao.findByConceptId(conceptId);

            ConceptGraph graph = conceptService.getConceptGraph(course.getCourseId());
            Set<Integer> prereqIds = graph.prerequisitesOf(conceptId);
            List<Concept> prereqConcepts = new ArrayList<>();
            for (int pId : prereqIds) {
                prereqConcepts.add(graph.getConcept(pId));
            }

            // Quizzes & Assignments for this course
            List<Quiz> quizzes = quizDao.findPublishedByCourseId(course.getCourseId());
            List<Assignment> assignments = assignmentDao.findByCourseId(course.getCourseId());

            // Mastery history timeline
            List<MasterySnapshot> history = masterySnapshotDao.findHistory(user.getId(), conceptId);

            req.setAttribute("concept", concept);
            req.setAttribute("course", course);
            req.setAttribute("statusView", statusView);
            req.setAttribute("materials", materials);
            req.setAttribute("prerequisites", prereqConcepts);
            req.setAttribute("quizzes", quizzes);
            req.setAttribute("assignments", assignments);
            req.setAttribute("history", history);

            forward(req, resp, "learn/concept-detail");

        } catch (Exception e) {
            setFlashError(req, "Concept not found.");
            redirect(req, resp, "/learn/home");
        }
    }
}
