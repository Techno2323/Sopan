package com.sopan.web.servlet;

import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.*;
import com.sopan.service.AssignmentService;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "InstructorAssignmentServlet", urlPatterns = {"/teach/assignments"})
public class InstructorAssignmentServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private AssignmentService assignmentService;
    private ConceptService conceptService;

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.assignmentService = (AssignmentService) getServletContext().getAttribute("assignmentService");
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Course> courses = courseService.getInstructorCourses(user.getId());

        int selectedCourseId = Validator.parseIntOrDefault(req.getParameter("course"), 0);
        if (selectedCourseId <= 0 && !courses.isEmpty()) {
            selectedCourseId = courses.get(0).getCourseId();
        }

        List<Assignment> assignments = new ArrayList<>();
        List<Concept> concepts = new ArrayList<>();
        Course selectedCourse = null;

        if (selectedCourseId > 0) {
            final int cId = selectedCourseId;
            selectedCourse = courses.stream().filter(c -> c.getCourseId() == cId).findFirst().orElse(null);
            if (selectedCourse != null) {
                assignments = assignmentService.getCourseAssignments(selectedCourseId);
                concepts = conceptService.getCourseConcepts(selectedCourseId);
            }
        }

        req.setAttribute("courses", courses);
        req.setAttribute("selectedCourse", selectedCourse);
        req.setAttribute("assignments", assignments);
        req.setAttribute("concepts", concepts);

        forward(req, resp, "teach/assignments");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);

        try {
            String title = req.getParameter("title");
            String instructions = req.getParameter("instructions");
            String dueAtStr = req.getParameter("dueAt");
            boolean allowLate = "on".equalsIgnoreCase(req.getParameter("allowLate")) || "true".equalsIgnoreCase(req.getParameter("allowLate"));

            LocalDateTime dueAt = LocalDateTime.parse(dueAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String[] descriptions = req.getParameterValues("criterionDescription");
            String[] maxPointsArr = req.getParameterValues("criterionMaxPoints");
            String[] conceptIdsArr = req.getParameterValues("criterionConceptId");

            if (descriptions == null || descriptions.length == 0) {
                throw new ValidationException("Please provide at least one rubric criterion.");
            }

            List<RubricCriterion> criteria = new ArrayList<>();
            for (int i = 0; i < descriptions.length; i++) {
                if (descriptions[i] != null && !descriptions[i].isBlank()) {
                    RubricCriterion rc = new RubricCriterion();
                    rc.setDescription(descriptions[i].trim());
                    rc.setMaxPoints(Integer.parseInt(maxPointsArr[i]));
                    rc.setConceptId(Integer.parseInt(conceptIdsArr[i]));
                    criteria.add(rc);
                }
            }

            assignmentService.createAssignment(courseId, user.getId(), title, instructions, dueAt, allowLate, criteria);
            setFlashSuccess(req, "Assignment and concept-linked rubric created successfully.");

        } catch (ValidationException | AccessDeniedException e) {
            setFlashError(req, e.getMessage());
        } catch (Exception e) {
            setFlashError(req, "Error creating assignment: " + e.getMessage());
        }

        redirect(req, resp, "/teach/assignments?course=" + courseId);
    }
}
