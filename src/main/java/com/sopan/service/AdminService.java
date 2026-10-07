package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.AuditLogDao;
import com.sopan.dao.CourseDao;
import com.sopan.dao.NotificationDao;
import com.sopan.dao.UserDao;
import com.sopan.model.AuditLog;
import com.sopan.model.Course;
import com.sopan.model.Notification;
import com.sopan.model.User;
import com.sopan.model.enums.AccountStatus;
import com.sopan.model.enums.CourseStatus;

import java.util.List;

public class AdminService {

    private final UserDao userDao;
    private final CourseDao courseDao;
    private final AuditLogDao auditLogDao;
    private final NotificationDao notificationDao;
    private final TransactionManager transactionManager;

    public AdminService(UserDao userDao, CourseDao courseDao, AuditLogDao auditLogDao,
                        NotificationDao notificationDao, TransactionManager transactionManager) {
        this.userDao = userDao;
        this.courseDao = courseDao;
        this.auditLogDao = auditLogDao;
        this.notificationDao = notificationDao;
        this.transactionManager = transactionManager;
    }

    public List<User> getAllUsers() {
        return userDao.findAllUsers();
    }

    public void updateUserStatus(int adminId, int targetUserId, AccountStatus status) {
        transactionManager.inTransactionVoid(conn -> {
            userDao.updateStatus(conn, targetUserId, status);
            auditLogDao.log(conn, adminId, "USER_STATUS_CHANGED_" + status.name(), "USER", targetUserId);

            Notification notif = new Notification();
            notif.setUserId(targetUserId);
            notif.setType("ACCOUNT_STATUS");
            notif.setMessage("Your account status has been changed to: " + status.name());
            notif.setDedupeKey("status_change_" + targetUserId + "_" + System.currentTimeMillis());
            notificationDao.saveIfNotExists(conn, notif);
        });
    }

    public List<Course> getPendingCourses() {
        return courseDao.findByStatus(CourseStatus.PENDING_APPROVAL);
    }

    public void approveCourse(int adminId, int courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        transactionManager.inTransactionVoid(conn -> {
            courseDao.updateStatus(conn, courseId, CourseStatus.PUBLISHED);
            auditLogDao.log(conn, adminId, "COURSE_APPROVED", "COURSE", courseId);

            Notification notif = new Notification();
            notif.setUserId(course.getInstructorId());
            notif.setType("COURSE_APPROVED");
            notif.setMessage("Congratulations! Your course '" + course.getTitle() + "' has been approved and published.");
            notif.setLink("/teach/home");
            notif.setDedupeKey("appr_course_" + courseId);
            notificationDao.saveIfNotExists(conn, notif);
        });
    }

    public void rejectCourse(int adminId, int courseId, String reason) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        transactionManager.inTransactionVoid(conn -> {
            courseDao.updateStatus(conn, courseId, CourseStatus.DRAFT);
            auditLogDao.log(conn, adminId, "COURSE_REJECTED", "COURSE", courseId);

            Notification notif = new Notification();
            notif.setUserId(course.getInstructorId());
            notif.setType("COURSE_REJECTED");
            notif.setMessage("Your course '" + course.getTitle() + "' was returned to draft: " + (reason != null ? reason : "Please revise."));
            notif.setLink("/teach/courses");
            notif.setDedupeKey("rej_course_" + courseId + "_" + System.currentTimeMillis());
            notificationDao.saveIfNotExists(conn, notif);
        });
    }

    public List<AuditLog> getAuditLogs(int limit) {
        return auditLogDao.findRecent(limit);
    }
}
