package com.sopan.dao;

import com.sopan.model.RubricCriterion;

import java.sql.Connection;
import java.util.List;

public interface RubricCriterionDao extends Dao<RubricCriterion, Integer> {

    List<RubricCriterion> findByAssignmentId(int assignmentId);
    List<RubricCriterion> findByAssignmentId(Connection conn, int assignmentId);
}
