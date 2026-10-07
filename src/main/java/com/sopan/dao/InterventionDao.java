package com.sopan.dao;

import com.sopan.model.Intervention;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public interface InterventionDao extends Dao<Intervention, Integer> {

    List<Intervention> findByCourseId(int courseId);
    List<Intervention> findByCourseId(Connection conn, int courseId);

    List<Intervention> findByStudentId(int studentId);
    List<Intervention> findByStudentId(Connection conn, int studentId);

    List<Intervention> findUnresolvedByStudentAndConcept(Connection conn, int studentId, int conceptId);

    void resolve(Connection conn, int interventionId, BigDecimal masteryAfter);
}
