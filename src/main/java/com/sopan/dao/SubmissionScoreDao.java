package com.sopan.dao;

import com.sopan.model.SubmissionScore;

import java.sql.Connection;
import java.util.List;

public interface SubmissionScoreDao {

    void saveScores(Connection conn, int submissionId, List<SubmissionScore> scores);
    List<SubmissionScore> findBySubmissionId(int submissionId);
    List<SubmissionScore> findBySubmissionId(Connection conn, int submissionId);
}
