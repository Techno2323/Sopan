package com.sopan.dao;

import com.sopan.model.Concept;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ConceptDao extends Dao<Concept, Integer> {

    List<Concept> findByCourseId(int courseId);
    List<Concept> findByCourseId(Connection conn, int courseId);

    Map<Integer, Set<Integer>> findPrerequisitesMap(int courseId);
    Map<Integer, Set<Integer>> findPrerequisitesMap(Connection conn, int courseId);

    Set<Integer> findPrerequisitesForConcept(Connection conn, int conceptId);
    Set<Integer> findPrerequisitesForConcept(int conceptId);

    void savePrerequisites(Connection conn, int courseId, Map<Integer, Set<Integer>> prerequisites);

    int countEvidenceForConcept(Connection conn, int conceptId);
    int countEvidenceForConcept(int conceptId);
}
