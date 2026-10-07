package com.sopan.dao;

import com.sopan.model.Submission;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface SubmissionDao extends Dao<Submission, Integer> {

    Optional<Submission> findByAssignmentAndStudent(int assignmentId, int studentId);
    Optional<Submission> findByAssignmentAndStudent(Connection conn, int assignmentId, int studentId);

    List<Submission> findByAssignmentId(int assignmentId);
    List<Submission> findByAssignmentId(Connection conn, int assignmentId);

    List<Submission> findPendingGradingByInstructor(int instructorId);
    List<Submission> findPendingGradingByInstructor(Connection conn, int instructorId);
}
