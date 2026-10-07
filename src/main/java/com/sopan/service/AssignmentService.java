package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.*;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.PrerequisiteNotMetException;
import com.sopan.exception.ValidationException;
import com.sopan.model.*;
import com.sopan.util.Validator;

import java.time.LocalDateTime;
import java.util.*;

public class AssignmentService {

    private final AssignmentDao assignmentDao;
    private final RubricCriterionDao rubricCriterionDao;
    private final SubmissionDao submissionDao;
    private final SubmissionScoreDao submissionScoreDao;
    private final CourseDao courseDao;
    private final MasteryService masteryService;
    private final InterventionDao interventionDao;
    private final NotificationDao notificationDao;
    private final AuditLogDao auditLogDao;
    private final TransactionManager transactionManager;

    public AssignmentService(AssignmentDao assignmentDao, RubricCriterionDao rubricCriterionDao,
                             SubmissionDao submissionDao, SubmissionScoreDao submissionScoreDao,
                             CourseDao courseDao, MasteryService masteryService,
                             InterventionDao interventionDao, NotificationDao notificationDao,
                             AuditLogDao auditLogDao, TransactionManager transactionManager) {
        this.assignmentDao = assignmentDao;
        this.rubricCriterionDao = rubricCriterionDao;
        this.submissionDao = submissionDao;
        this.submissionScoreDao = submissionScoreDao;
        this.courseDao = courseDao;
        this.masteryService = masteryService;
        this.interventionDao = interventionDao;
        this.notificationDao = notificationDao;
        this.auditLogDao = auditLogDao;
        this.transactionManager = transactionManager;
    }

    public Assignment getAssignment(int assignmentId) {
        return assignmentDao.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
    }

    public List<Assignment> getCourseAssignments(int courseId) {
        return assignmentDao.findByCourseId(courseId);
    }

    public List<RubricCriterion> getAssignmentCriteria(int assignmentId) {
        return rubricCriterionDao.findByAssignmentId(assignmentId);
    }

    public Optional<Submission> getStudentSubmission(int assignmentId, int studentId) {
        return submissionDao.findByAssignmentAndStudent(assignmentId, studentId);
    }

    public List<Submission> getSubmissionsForAssignment(int assignmentId, int instructorId)
            throws AccessDeniedException {
        Assignment assignment = getAssignment(assignmentId);
        Course course = courseDao.findById(assignment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }
        return submissionDao.findByAssignmentId(assignmentId);
    }

    public List<Submission> getPendingGrading(int instructorId) {
        return submissionDao.findPendingGradingByInstructor(instructorId);
    }

    public int createAssignment(int courseId, int instructorId, String title, String instructions,
                                LocalDateTime dueAt, boolean allowLate, List<RubricCriterion> criteria)
            throws AccessDeniedException, ValidationException {

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        Validator.requireNotBlank(title, "Assignment title");
        if (dueAt == null) {
            throw new ValidationException("Due date is required.");
        }
        if (criteria == null || criteria.isEmpty()) {
            throw new ValidationException("Assignment must have at least one rubric criterion.");
        }

        for (RubricCriterion rc : criteria) {
            Validator.requireNotBlank(rc.getDescription(), "Criterion description");
            if (rc.getMaxPoints() <= 0) {
                throw new ValidationException("Rubric criterion points must be positive.");
            }
            if (rc.getConceptId() <= 0) {
                throw new ValidationException("Rubric criterion must be tagged to a concept.");
            }
        }

        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setTitle(title.trim());
        assignment.setInstructions(instructions != null ? instructions.trim() : "");
        assignment.setDueAt(dueAt);
        assignment.setAllowLate(allowLate);

        return transactionManager.inTransaction(conn -> {
            int aId = assignmentDao.save(conn, assignment);
            for (RubricCriterion rc : criteria) {
                rc.setAssignmentId(aId);
                rubricCriterionDao.save(conn, rc);
            }
            auditLogDao.log(conn, instructorId, "ASSIGNMENT_CREATED", "ASSIGNMENT", aId);
            return aId;
        });
    }

    /**
     * Submits or edits an assignment before deadline or within late submission permissions.
     * Validates prerequisite gating on all criteria concepts.
     */
    public int submitAssignment(int studentId, int assignmentId, String bodyText, String filePath)
            throws PrerequisiteNotMetException, ValidationException {

        Assignment assignment = getAssignment(assignmentId);
        int courseId = assignment.getCourseId();

        // Gating Check: Ensure no rubric criterion belongs to a locked concept
        List<RubricCriterion> criteria = rubricCriterionDao.findByAssignmentId(assignmentId);
        for (RubricCriterion rc : criteria) {
            masteryService.checkPrerequisitesMet(studentId, courseId, rc.getConceptId());
        }

        boolean isLate = LocalDateTime.now().isAfter(assignment.getDueAt());
        if (isLate && !assignment.isAllowLate()) {
            throw new ValidationException("The deadline has passed and late submissions are not allowed for this assignment.");
        }

        return transactionManager.inTransaction(conn -> {
            Optional<Submission> existingOpt = submissionDao.findByAssignmentAndStudent(conn, assignmentId, studentId);
            int subId;

            if (existingOpt.isPresent()) {
                Submission existing = existingOpt.get();
                if ("GRADED".equals(existing.getStatus())) {
                    throw new ValidationException("This submission has already been graded and cannot be modified.");
                }
                existing.setBodyText(bodyText);
                if (filePath != null && !filePath.isBlank()) {
                    existing.setFilePath(filePath);
                }
                existing.setLate(isLate);
                existing.setStatus("SUBMITTED");
                submissionDao.update(conn, existing);
                subId = existing.getSubmissionId();
            } else {
                Submission sub = new Submission();
                sub.setAssignmentId(assignmentId);
                sub.setStudentId(studentId);
                sub.setBodyText(bodyText);
                sub.setFilePath(filePath);
                sub.setLate(isLate);
                sub.setStatus("SUBMITTED");
                subId = submissionDao.save(conn, sub);
            }

            auditLogDao.log(conn, studentId, "ASSIGNMENT_SUBMITTED", "SUBMISSION", subId);
            return subId;
        });
    }

    /**
     * Grades a submission criterion-by-criterion. Validates points bounds, updates status,
     * appends mastery snapshots, resolves pending interventions, and records an audit log.
     */
    public void gradeSubmission(int submissionId, int instructorId, Map<Integer, Integer> pointsMap, String feedback)
            throws AccessDeniedException, ValidationException {

        Submission submission = submissionDao.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));
        Assignment assignment = getAssignment(submission.getAssignmentId());
        Course course = courseDao.findById(assignment.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        List<RubricCriterion> criteria = rubricCriterionDao.findByAssignmentId(assignment.getAssignmentId());
        List<SubmissionScore> scoreList = new ArrayList<>();
        Set<Integer> affectedConcepts = new HashSet<>();

        for (RubricCriterion rc : criteria) {
            Integer points = pointsMap.get(rc.getCriterionId());
            if (points == null) {
                throw new ValidationException("Missing score for criterion: " + rc.getDescription());
            }
            if (points < 0 || points > rc.getMaxPoints()) {
                throw new ValidationException(String.format("Points for '%s' must be between 0 and %d (received %d).",
                        rc.getDescription(), rc.getMaxPoints(), points));
            }

            SubmissionScore ss = new SubmissionScore();
            ss.setSubmissionId(submissionId);
            ss.setCriterionId(rc.getCriterionId());
            ss.setPoints(points);
            scoreList.add(ss);
            affectedConcepts.add(rc.getConceptId());
        }

        transactionManager.inTransactionVoid(conn -> {
            // 1. Save rubric scores
            submissionScoreDao.saveScores(conn, submissionId, scoreList);

            // 2. Mark submission GRADED
            submission.setStatus("GRADED");
            submission.setGradedAt(LocalDateTime.now());
            submission.setFeedback(feedback != null ? feedback.trim() : "");
            submissionDao.update(conn, submission);

            // 3. Recompute mastery snapshots for all affected concepts
            masteryService.recomputeForConcepts(conn, submission.getStudentId(), affectedConcepts);

            // 4. Resolve any pending interventions
            for (int cId : affectedConcepts) {
                List<Intervention> pending = interventionDao.findUnresolvedByStudentAndConcept(conn, submission.getStudentId(), cId);
                if (!pending.isEmpty()) {
                    var latest = masteryService.recomputeAndSaveSnapshot(conn, submission.getStudentId(), cId);
                    for (Intervention inv : pending) {
                        interventionDao.resolve(conn, inv.getInterventionId(), latest.getMastery());
                    }
                }
            }

            // 5. Audit & Notification
            auditLogDao.log(conn, instructorId, "SUBMISSION_GRADED", "SUBMISSION", submissionId);

            Notification notif = new Notification();
            notif.setUserId(submission.getStudentId());
            notif.setType("ASSIGNMENT_GRADED");
            notif.setMessage(String.format("Your submission for '%s' has been graded.", assignment.getTitle()));
            notif.setLink("/learn/assignment?id=" + assignment.getAssignmentId());
            notif.setDedupeKey("assign_graded_" + submissionId);
            notificationDao.saveIfNotExists(conn, notif);
        });
    }

    public List<SubmissionScore> getSubmissionScores(int submissionId) {
        return submissionScoreDao.findBySubmissionId(submissionId);
    }
}
