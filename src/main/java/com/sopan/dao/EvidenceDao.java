package com.sopan.dao;

import com.sopan.model.evidence.Evidence;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface EvidenceDao {

    List<Evidence> findByStudentAndConcept(int studentId, int conceptId);
    List<Evidence> findByStudentAndConcept(Connection conn, int studentId, int conceptId);

    Map<Integer, List<Evidence>> findByStudentAndCourse(int studentId, int courseId);
    Map<Integer, List<Evidence>> findByStudentAndCourse(Connection conn, int studentId, int courseId);
}
