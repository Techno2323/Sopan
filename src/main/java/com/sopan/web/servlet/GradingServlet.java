package com.sopan.web.servlet;

import com.sopan.dao.SubmissionDao;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.*;
import com.sopan.service.AssignmentService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "GradingServlet", urlPatterns = {"/teach/grade"})
public class GradingServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private AssignmentService assignmentService;
    private SubmissionDao submissionDao;

    @Override
    public void init() throws ServletException {
        this.assignmentService = (AssignmentService) getServletContext().getAttribute("assignmentService");
        this.submissionDao = (SubmissionDao) getServletContext().getAttribute("submissionDao");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int submissionId = Validator.parseIntOrDefault(req.getParameter("submission"), 0);

        if (submissionId <= 0) {
            redirect(req, resp, "/teach/home");
            return;
        }

        try {
            Submission submission = submissionDao.findById(submissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
            Assignment assignment = assignmentService.getAssignment(submission.getAssignmentId());
            List<RubricCriterion> criteria = assignmentService.getAssignmentCriteria(assignment.getAssignmentId());
            List<SubmissionScore> scores = assignmentService.getSubmissionScores(submissionId);

            Map<Integer, Integer> scoreMap = new HashMap<>();
            for (SubmissionScore ss : scores) {
                scoreMap.put(ss.getCriterionId(), ss.getPoints());
            }

            req.setAttribute("submission", submission);
            req.setAttribute("assignment", assignment);
            req.setAttribute("criteria", criteria);
            req.setAttribute("scoreMap", scoreMap);

            forward(req, resp, "teach/grade");

        } catch (Exception e) {
            setFlashError(req, "Error loading submission: " + e.getMessage());
            redirect(req, resp, "/teach/home");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int submissionId = Validator.parseIntOrDefault(req.getParameter("submissionId"), 0);

        if (submissionId <= 0) {
            redirect(req, resp, "/teach/home");
            return;
        }

        Map<Integer, Integer> pointsMap = new HashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            if (name.startsWith("points_")) {
                int critId = Integer.parseInt(name.substring(7));
                int pts = Validator.parseIntOrDefault(req.getParameter(name), 0);
                pointsMap.put(critId, pts);
            }
        }

        String feedback = req.getParameter("feedback");

        try {
            assignmentService.gradeSubmission(submissionId, user.getId(), pointsMap, feedback);
            setFlashSuccess(req, "Submission graded and concept mastery updated!");
            redirect(req, resp, "/teach/home");

        } catch (ValidationException | AccessDeniedException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/teach/grade?submission=" + submissionId);
        }
    }
}
