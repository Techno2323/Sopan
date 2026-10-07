package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.AuditLogDao;
import com.sopan.dao.CourseDao;
import com.sopan.dao.EnrollmentDao;
import com.sopan.dao.NotificationDao;
import com.sopan.exception.EnrollmentException;
import com.sopan.model.Course;
import com.sopan.model.Enrollment;
import com.sopan.model.Notification;
import com.sopan.model.enums.CourseStatus;

import java.util.List;
import java.util.Optional;

public class EnrollmentService {

    private final EnrollmentDao enrollmentDao;
    private final CourseDao courseDao;
    private final NotificationDao notificationDao;
    private final AuditLogDao auditLogDao;
    private final TransactionManager transactionManager;

    public EnrollmentService(EnrollmentDao enrollmentDao, CourseDao courseDao,
                             NotificationDao notificationDao, AuditLogDao auditLogDao,
                             TransactionManager transactionManager) {
        this.enrollmentDao = enrollmentDao;
        this.courseDao = courseDao;
        this.notificationDao = notificationDao;
        this.auditLogDao = auditLogDao;
        this.transactionManager = transactionManager;
    }

    public void enroll(int courseId, int studentId) throws EnrollmentException {
        transactionManager.inTransactionVoid(conn -> {
            // 1. Lock the course row with SELECT ... FOR UPDATE
            Course course = courseDao.lockCourseForUpdate(conn, courseId)
                    .orElseThrow(() -> new EnrollmentException("Course not found: " + courseId));

            // 2. Only PUBLISHED courses can accept enrollments
            if (course.getStatus() != CourseStatus.PUBLISHED) {
                throw new EnrollmentException("Enrollment is closed because this course is not published.");
            }

            // 3. Check existing enrollment
            Optional<Enrollment> existingOpt = enrollmentDao.findByCourseAndStudent(conn, courseId, studentId);
            if (existingOpt.isPresent()) {
                Enrollment existing = existingOpt.get();
                if (existing.getStatus() == Enrollment.Status.ACTIVE) {
                    throw new EnrollmentException("You are already actively enrolled in this course.");
                } else if (existing.getStatus() == Enrollment.Status.DROPPED) {
                    // Check capacity before reactivating
                    int activeCount = enrollmentDao.countActiveByCourseId(conn, courseId);
                    if (activeCount >= course.getMaxStudents()) {
                        throw new EnrollmentException("Course capacity has been reached (" + course.getMaxStudents() + " students max).");
                    }
                    enrollmentDao.reactivate(conn, existing.getEnrollmentId());
                    auditLogDao.log(conn, studentId, "ENROLLMENT_REACTIVATED", "ENROLLMENT", existing.getEnrollmentId());
                    return;
                }
            }

            // 4. Check course capacity
            int activeCount = enrollmentDao.countActiveByCourseId(conn, courseId);
            if (activeCount >= course.getMaxStudents()) {
                throw new EnrollmentException("Course is full. Maximum enrollment capacity is " + course.getMaxStudents() + ".");
            }

            // 5. Insert new active enrollment
            Enrollment enrollment = new Enrollment();
            enrollment.setCourseId(courseId);
            enrollment.setStudentId(studentId);
            enrollment.setStatus(Enrollment.Status.ACTIVE);
            enrollmentDao.save(conn, enrollment);

            // 6. Audit & welcome notification
            auditLogDao.log(conn, studentId, "STUDENT_ENROLLED", "COURSE", courseId);

            Notification notif = new Notification();
            notif.setUserId(studentId);
            notif.setType("ENROLLMENT_SUCCESS");
            notif.setMessage("Successfully enrolled in " + course.getTitle() + " (" + course.getCode() + ").");
            notif.setLink("/learn/course/map?id=" + courseId);
            notif.setDedupeKey("enroll_" + courseId + "_" + studentId);
            notificationDao.saveIfNotExists(conn, notif);
        });
    }

    public void drop(int courseId, int studentId) throws EnrollmentException {
        transactionManager.inTransactionVoid(conn -> {
            Enrollment enrollment = enrollmentDao.findByCourseAndStudent(conn, courseId, studentId)
                    .orElseThrow(() -> new EnrollmentException("You are not enrolled in this course."));

            if (enrollment.getStatus() != Enrollment.Status.ACTIVE) {
                throw new EnrollmentException("Enrollment is not currently active.");
            }

            enrollmentDao.updateStatus(conn, enrollment.getEnrollmentId(), Enrollment.Status.DROPPED);
            auditLogDao.log(conn, studentId, "STUDENT_DROPPED", "COURSE", courseId);
        });
    }

    public List<Enrollment> getStudentEnrollments(int studentId) {
        return enrollmentDao.findByStudentId(studentId);
    }

    public List<Enrollment> getCourseEnrollments(int courseId) {
        return enrollmentDao.findByCourseId(courseId);
    }

    public boolean isStudentEnrolled(int courseId, int studentId) {
        return enrollmentDao.findByCourseAndStudent(courseId, studentId)
                .map(e -> e.getStatus() == Enrollment.Status.ACTIVE)
                .orElse(false);
    }
}
