package com.sopan.dao;

import com.sopan.model.Enrollment;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface EnrollmentDao extends Dao<Enrollment, Integer> {

    Optional<Enrollment> findByCourseAndStudent(int courseId, int studentId);
    Optional<Enrollment> findByCourseAndStudent(Connection conn, int courseId, int studentId);

    List<Enrollment> findByStudentId(int studentId);
    List<Enrollment> findByStudentId(Connection conn, int studentId);

    List<Enrollment> findByCourseId(int courseId);
    List<Enrollment> findByCourseId(Connection conn, int courseId);

    int countActiveByCourseId(Connection conn, int courseId);
    int countActiveByCourseId(int courseId);

    void reactivate(Connection conn, int enrollmentId);
    void updateStatus(Connection conn, int enrollmentId, Enrollment.Status status);
}
