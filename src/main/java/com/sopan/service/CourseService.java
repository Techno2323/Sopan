package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.AuditLogDao;
import com.sopan.dao.ConceptDao;
import com.sopan.dao.CourseDao;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Course;
import com.sopan.model.enums.CourseStatus;
import com.sopan.model.enums.Difficulty;
import com.sopan.util.Page;
import com.sopan.util.Validator;

import java.util.List;

public class CourseService {

    private final CourseDao courseDao;
    private final ConceptDao conceptDao;
    private final AuditLogDao auditLogDao;
    private final TransactionManager transactionManager;

    public CourseService(CourseDao courseDao, ConceptDao conceptDao,
                         AuditLogDao auditLogDao, TransactionManager transactionManager) {
        this.courseDao = courseDao;
        this.conceptDao = conceptDao;
        this.auditLogDao = auditLogDao;
        this.transactionManager = transactionManager;
    }

    public Course getCourse(int courseId) {
        return courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
    }

    public List<Course> getInstructorCourses(int instructorId) {
        return courseDao.findByInstructorId(instructorId);
    }

    public Page<Course> searchPublishedCourses(String query, String category, String difficulty,
                                               String sortBy, String sortOrder, int pageNumber, int pageSize) {
        return courseDao.searchPublished(query, category, difficulty, sortBy, sortOrder, pageNumber, pageSize);
    }

    public int createCourse(int instructorId, String code, String title, String description,
                            String category, Difficulty difficulty, int maxStudents, int gateThreshold)
            throws ValidationException {

        Validator.requireNotBlank(code, "Course code");
        Validator.requireNotBlank(title, "Course title");
        Validator.validateRange(gateThreshold, 0, 100, "Gate threshold");
        if (maxStudents <= 0) {
            throw new ValidationException("Maximum students must be greater than zero.");
        }

        Course course = new Course();
        course.setInstructorId(instructorId);
        course.setCode(code.trim().toUpperCase());
        course.setTitle(title.trim());
        course.setDescription(description != null ? description.trim() : "");
        course.setCategory(category != null ? category.trim() : "General");
        course.setDifficulty(difficulty != null ? difficulty : Difficulty.BEGINNER);
        course.setMaxStudents(maxStudents);
        course.setGateThreshold(gateThreshold);
        course.setStatus(CourseStatus.DRAFT);

        return transactionManager.inTransaction(conn -> {
            int id = courseDao.save(conn, course);
            auditLogDao.log(conn, instructorId, "COURSE_CREATED", "COURSE", id);
            return id;
        });
    }

    public void updateCourse(int courseId, int instructorId, String title, String description,
                             String category, Difficulty difficulty, int maxStudents, int gateThreshold)
            throws ValidationException, AccessDeniedException {

        Course course = getCourse(courseId);
        checkInstructorOwnership(course, instructorId);

        Validator.requireNotBlank(title, "Course title");
        Validator.validateRange(gateThreshold, 0, 100, "Gate threshold");
        if (maxStudents <= 0) {
            throw new ValidationException("Maximum students must be greater than zero.");
        }

        course.setTitle(title.trim());
        course.setDescription(description != null ? description.trim() : "");
        course.setCategory(category != null ? category.trim() : "General");
        course.setDifficulty(difficulty != null ? difficulty : Difficulty.BEGINNER);
        course.setMaxStudents(maxStudents);
        course.setGateThreshold(gateThreshold);

        transactionManager.inTransactionVoid(conn -> {
            courseDao.update(conn, course);
            auditLogDao.log(conn, instructorId, "COURSE_UPDATED", "COURSE", courseId);
        });
    }

    public void submitForApproval(int courseId, int instructorId) throws AccessDeniedException, ValidationException {
        Course course = getCourse(courseId);
        checkInstructorOwnership(course, instructorId);

        // Validation rule: A course cannot be submitted without at least one concept
        int conceptCount = conceptDao.findByCourseId(courseId).size();
        if (conceptCount == 0) {
            throw new ValidationException("Cannot submit course for approval without at least one concept.");
        }

        if (course.getStatus() != CourseStatus.DRAFT) {
            throw new ValidationException("Only draft courses can be submitted for approval.");
        }

        transactionManager.inTransactionVoid(conn -> {
            courseDao.updateStatus(conn, courseId, CourseStatus.PENDING_APPROVAL);
            auditLogDao.log(conn, instructorId, "COURSE_SUBMITTED_FOR_APPROVAL", "COURSE", courseId);
        });
    }

    public void deleteOrArchiveCourse(int courseId, int instructorId) throws AccessDeniedException {
        Course course = getCourse(courseId);
        checkInstructorOwnership(course, instructorId);

        transactionManager.inTransactionVoid(conn -> {
            boolean hasEnrollments = courseDao.hasEnrollments(conn, courseId);
            if (hasEnrollments) {
                // Rule: A course with enrollments is archived, never hard deleted
                courseDao.updateStatus(conn, courseId, CourseStatus.ARCHIVED);
                auditLogDao.log(conn, instructorId, "COURSE_ARCHIVED", "COURSE", courseId);
            } else {
                courseDao.delete(conn, courseId);
                auditLogDao.log(conn, instructorId, "COURSE_DELETED", "COURSE", courseId);
            }
        });
    }

    public void checkInstructorOwnership(Course course, int instructorId) throws AccessDeniedException {
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not have permission to manage this course.");
        }
    }
}
