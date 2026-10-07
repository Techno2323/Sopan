package com.sopan.web.servlet;

import com.sopan.exception.PrerequisiteNotMetException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Assignment;
import com.sopan.model.RubricCriterion;
import com.sopan.model.Submission;
import com.sopan.model.SubmissionScore;
import com.sopan.model.User;
import com.sopan.service.AssignmentService;
import com.sopan.service.EnrollmentService;
import com.sopan.util.FileStorage;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AssignmentServlet", urlPatterns = {"/learn/assignment"})
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,        // 5 MB max
        maxRequestSize = 6 * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class AssignmentServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private AssignmentService assignmentService;
    private EnrollmentService enrollmentService;
    private FileStorage fileStorage;

    @Override
    public void init() throws ServletException {
        this.assignmentService = (AssignmentService) getServletContext().getAttribute("assignmentService");
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
        this.fileStorage = (FileStorage) getServletContext().getAttribute("fileStorage");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int assignmentId = Validator.parseIntOrDefault(req.getParameter("id"), 0);

        if (assignmentId <= 0) {
            redirect(req, resp, "/learn/home");
            return;
        }

        try {
            Assignment assignment = assignmentService.getAssignment(assignmentId);

            if (!enrollmentService.isStudentEnrolled(assignment.getCourseId(), user.getId())) {
                setFlashError(req, "You must be enrolled in this course to view assignments.");
                redirect(req, resp, "/catalog/course?id=" + assignment.getCourseId());
                return;
            }

            List<RubricCriterion> criteria = assignmentService.getAssignmentCriteria(assignmentId);
            Optional<Submission> submissionOpt = assignmentService.getStudentSubmission(assignmentId, user.getId());

            req.setAttribute("assignment", assignment);
            req.setAttribute("criteria", criteria);

            if (submissionOpt.isPresent()) {
                Submission sub = submissionOpt.get();
                req.setAttribute("submission", sub);
                if ("GRADED".equals(sub.getStatus())) {
                    List<SubmissionScore> scores = assignmentService.getSubmissionScores(sub.getSubmissionId());
                    req.setAttribute("scores", scores);
                }
            }

            forward(req, resp, "learn/assignment");

        } catch (Exception e) {
            setFlashError(req, "Assignment not found.");
            redirect(req, resp, "/learn/home");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int assignmentId = Validator.parseIntOrDefault(req.getParameter("assignmentId"), 0);

        if (assignmentId <= 0) {
            redirect(req, resp, "/learn/home");
            return;
        }

        String bodyText = req.getParameter("bodyText");
        String filePath = null;

        try {
            Part filePart = req.getPart("file");
            if (filePart != null && filePart.getSize() > 0) {
                try (InputStream in = filePart.getInputStream()) {
                    filePath = fileStorage.savePdf(in, filePart.getSubmittedFileName());
                }
            }

            assignmentService.submitAssignment(user.getId(), assignmentId, bodyText, filePath);
            setFlashSuccess(req, "Assignment submitted successfully!");
            redirect(req, resp, "/learn/assignment?id=" + assignmentId);

        } catch (PrerequisiteNotMetException | ValidationException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/learn/assignment?id=" + assignmentId);
        } catch (Exception e) {
            setFlashError(req, "Error uploading submission: " + e.getMessage());
            redirect(req, resp, "/learn/assignment?id=" + assignmentId);
        }
    }
}
