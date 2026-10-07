package com.sopan.concurrent;

import com.sopan.dao.AssignmentDao;
import com.sopan.dao.EnrollmentDao;
import com.sopan.dao.NotificationDao;
import com.sopan.dao.SubmissionDao;
import com.sopan.model.Assignment;
import com.sopan.model.Enrollment;
import com.sopan.model.Notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduled scanner that scans upcoming assignment deadlines
 * and alerts enrolled students who have not yet submitted.
 */
public class DeadlineScanner implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(DeadlineScanner.class.getName());

    private final AssignmentDao assignmentDao;
    private final EnrollmentDao enrollmentDao;
    private final SubmissionDao submissionDao;
    private final NotificationDao notificationDao;

    public DeadlineScanner(AssignmentDao assignmentDao, EnrollmentDao enrollmentDao,
                           SubmissionDao submissionDao, NotificationDao notificationDao) {
        this.assignmentDao = assignmentDao;
        this.enrollmentDao = enrollmentDao;
        this.submissionDao = submissionDao;
        this.notificationDao = notificationDao;
    }

    @Override
    public void run() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Assignment> assignments = assignmentDao.findAll();

            for (Assignment a : assignments) {
                if (a.getDueAt() == null) continue;
                Duration diff = Duration.between(now, a.getDueAt());
                long hours = diff.toHours();

                // Alert if due within 24 hours
                if (hours >= 0 && hours <= 24) {
                    List<Enrollment> enrollments = enrollmentDao.findByCourseId(a.getCourseId());
                    for (Enrollment e : enrollments) {
                        if (e.getStatus() != Enrollment.Status.ACTIVE) continue;
                        int studentId = e.getStudentId();

                        // Check if student has submitted
                        boolean submitted = submissionDao.findByAssignmentAndStudent(a.getAssignmentId(), studentId).isPresent();
                        if (!submitted) {
                            Notification notif = new Notification();
                            notif.setUserId(studentId);
                            notif.setType("DEADLINE_REMINDER");
                            notif.setMessage(String.format("Assignment '%s' is due in %d hours!", a.getTitle(), hours));
                            notif.setLink("/learn/assignment?id=" + a.getAssignmentId());
                            notif.setDedupeKey("deadline_" + a.getAssignmentId() + "_" + studentId);
                            notificationDao.saveIfNotExists(null, notif);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error scanning assignment deadlines", e);
        }
    }
}
