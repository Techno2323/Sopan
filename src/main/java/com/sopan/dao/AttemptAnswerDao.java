package com.sopan.dao;

import com.sopan.model.AttemptAnswer;

import java.sql.Connection;
import java.util.List;

public interface AttemptAnswerDao {

    void saveAll(Connection conn, List<AttemptAnswer> answers);
    List<AttemptAnswer> findByAttemptId(int attemptId);
    List<AttemptAnswer> findByAttemptId(Connection conn, int attemptId);
}
