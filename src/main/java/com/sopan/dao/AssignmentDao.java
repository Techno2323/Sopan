package com.sopan.dao;

import com.sopan.model.Assignment;

import java.sql.Connection;
import java.util.List;

public interface AssignmentDao extends Dao<Assignment, Integer> {

    List<Assignment> findByCourseId(int courseId);
    List<Assignment> findByCourseId(Connection conn, int courseId);

    List<Assignment> findUpcomingByStudentId(int studentId, int limit);
    List<Assignment> findUpcomingByStudentId(Connection conn, int studentId, int limit);
}
