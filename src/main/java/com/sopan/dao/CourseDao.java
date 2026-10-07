package com.sopan.dao;

import com.sopan.model.Course;
import com.sopan.model.enums.CourseStatus;
import com.sopan.util.Page;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface CourseDao extends Dao<Course, Integer> {

    List<Course> findByInstructorId(int instructorId);
    List<Course> findByInstructorId(Connection conn, int instructorId);

    List<Course> findByStatus(CourseStatus status);
    List<Course> findByStatus(Connection conn, CourseStatus status);

    Optional<Course> lockCourseForUpdate(Connection conn, int courseId);

    Page<Course> searchPublished(String query, String category, String difficulty,
                                 String sortBy, String sortOrder, int pageNumber, int pageSize);

    void updateStatus(Connection conn, int courseId, CourseStatus status);
    void updateStatus(int courseId, CourseStatus status);

    boolean hasEnrollments(Connection conn, int courseId);
    boolean hasEnrollments(int courseId);
}
