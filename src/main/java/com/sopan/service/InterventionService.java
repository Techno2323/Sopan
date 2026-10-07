package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.*;
import com.sopan.dto.CohortRow;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.*;
import com.sopan.model.enums.MasteryLevel;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InterventionService {

    private final InterventionDao interventionDao;
    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final ConceptDao conceptDao;
    private final MasterySnapshotDao masterySnapshotDao;
    private final AuditLogDao auditLogDao;
    private final NotificationDao notificationDao;
    private final TransactionManager transactionManager;

    public InterventionService(InterventionDao interventionDao, CourseDao courseDao,
                               EnrollmentDao enrollmentDao, ConceptDao conceptDao,
                               MasterySnapshotDao masterySnapshotDao, AuditLogDao auditLogDao,
                               NotificationDao notificationDao, TransactionManager transactionManager) {
        this.interventionDao = interventionDao;
        this.courseDao = courseDao;
        this.enrollmentDao = enrollmentDao;
        this.conceptDao = conceptDao;
        this.masterySnapshotDao = masterySnapshotDao;
        this.auditLogDao = auditLogDao;
        this.notificationDao = notificationDao;
        this.transactionManager = transactionManager;
    }

    public List<Intervention> getCourseInterventions(int courseId, int instructorId) throws AccessDeniedException {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }
        return interventionDao.findByCourseId(courseId);
    }

    public int logIntervention(int courseId, int instructorId, int studentId, int conceptId,
                              Intervention.Type type, String note) throws AccessDeniedException, ValidationException {

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }
        if (type == null) {
            throw new ValidationException("Intervention type is required.");
        }

        // Current mastery recorded as mastery_before
        Optional<MasterySnapshot> snapOpt = masterySnapshotDao.findLatest(studentId, conceptId);
        BigDecimal currentMastery = snapOpt.map(MasterySnapshot::getMastery).orElse(BigDecimal.ZERO);

        Intervention inv = new Intervention();
        inv.setCourseId(courseId);
        inv.setInstructorId(instructorId);
        inv.setStudentId(studentId);
        inv.setConceptId(conceptId);
        inv.setType(type);
        inv.setNote(note != null ? note.trim() : "");
        inv.setMasteryBefore(currentMastery);

        return transactionManager.inTransaction(conn -> {
            int id = interventionDao.save(conn, inv);
            auditLogDao.log(conn, instructorId, "INTERVENTION_LOGGED", "INTERVENTION", id);

            Notification notif = new Notification();
            notif.setUserId(studentId);
            notif.setType("INTERVENTION");
            notif.setMessage("Your instructor scheduled an academic intervention: " + type.name().replace('_', ' '));
            notif.setLink("/learn/concept?id=" + conceptId);
            notif.setDedupeKey("inv_" + id);
            notificationDao.saveIfNotExists(conn, notif);

            return id;
        });
    }

    /**
     * Builds the cohort heatmap matrix for all enrolled students across all concepts,
     * calculating who is currently stalled.
     * Stalled definition:
     * 1. evidenceCount >= 3 and mastery < 50
     * OR
     * 2. mastery has not improved by >= 5 points over 14 days despite >= 2 new evidence items
     */
    public List<CohortRow> getCohortHeatmap(int courseId, int instructorId) throws AccessDeniedException {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        List<Enrollment> enrollments = enrollmentDao.findByCourseId(courseId);
        List<Concept> concepts = conceptDao.findByCourseId(courseId);

        List<CohortRow> rows = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Enrollment e : enrollments) {
            if (e.getStatus() != Enrollment.Status.ACTIVE) continue;

            int studentId = e.getStudentId();
            Map<Integer, MasterySnapshot> latestMap = masterySnapshotDao.findLatestForCourse(studentId, courseId);

            Map<Integer, Double> masteryMap = new HashMap<>();
            Map<Integer, MasteryLevel> levelMap = new HashMap<>();
            int stalledCount = 0;

            for (Concept c : concepts) {
                int cId = c.getConceptId();
                MasterySnapshot latest = latestMap.get(cId);

                if (latest != null && latest.getEvidenceCount() > 0) {
                    double m = latest.getMastery().doubleValue();
                    int count = latest.getEvidenceCount();
                    masteryMap.put(cId, m);

                    MasteryLevel level;
                    if (count < 3) level = MasteryLevel.EARLY;
                    else if (m < 50.0) level = MasteryLevel.SHAKY;
                    else if (m < 75.0) level = MasteryLevel.DEVELOPING;
                    else level = MasteryLevel.SOLID;

                    levelMap.put(cId, level);

                    // Check stalled rule 1: evidenceCount >= 3 and mastery < 50
                    boolean stalled = (count >= 3 && m < 50.0);

                    // Check stalled rule 2: history check over 14 days
                    if (!stalled) {
                        List<MasterySnapshot> history = masterySnapshotDao.findHistory(studentId, cId);
                        if (history.size() >= 2) {
                            MasterySnapshot earliestInWindow = null;
                            for (MasterySnapshot h : history) {
                                if (Duration.between(h.getComputedAt(), now).toDays() <= 14) {
                                    if (earliestInWindow == null || h.getComputedAt().isBefore(earliestInWindow.getComputedAt())) {
                                        earliestInWindow = h;
                                    }
                                }
                            }
                            if (earliestInWindow != null) {
                                int newEvidence = latest.getEvidenceCount() - earliestInWindow.getEvidenceCount();
                                double improvement = latest.getMastery().doubleValue() - earliestInWindow.getMastery().doubleValue();
                                if (newEvidence >= 2 && improvement < 5.0) {
                                    stalled = true;
                                }
                            }
                        }
                    }

                    if (stalled) {
                        stalledCount++;
                    }
                } else {
                    levelMap.put(cId, MasteryLevel.UNSEEN);
                }
            }

            rows.add(new CohortRow(studentId, e.getStudentName(), e.getStudentRollNo(),
                    masteryMap, levelMap, stalledCount > 0, stalledCount));
        }

        return rows;
    }
}
