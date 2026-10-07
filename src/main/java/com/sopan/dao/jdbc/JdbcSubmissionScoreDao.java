package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.SubmissionScoreDao;
import com.sopan.exception.DaoException;
import com.sopan.model.SubmissionScore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcSubmissionScoreDao extends BaseJdbcDao implements SubmissionScoreDao {

    public JdbcSubmissionScoreDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public void saveScores(Connection conn, int submissionId, List<SubmissionScore> scores) {
        if (scores == null || scores.isEmpty()) return;
        // Clear previous scores if re-grading
        String deleteSql = "DELETE FROM submission_scores WHERE submission_id = ?";
        try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
            delStmt.setInt(1, submissionId);
            delStmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to clear previous submission scores: " + submissionId, e);
        }

        String insertSql = "INSERT INTO submission_scores (submission_id, criterion_id, points) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            for (SubmissionScore score : scores) {
                stmt.setInt(1, submissionId);
                stmt.setInt(2, score.getCriterionId());
                stmt.setInt(3, score.getPoints());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DaoException("Failed to save submission scores for submission: " + submissionId, e);
        }
    }

    @Override
    public List<SubmissionScore> findBySubmissionId(int submissionId) {
        return execute(conn -> findBySubmissionId(conn, submissionId), "Failed to find submission scores");
    }

    @Override
    public List<SubmissionScore> findBySubmissionId(Connection conn, int submissionId) {
        String sql = "SELECT ss.submission_id, ss.criterion_id, ss.points, " +
                     "       rc.concept_id, rc.description AS criterion_description, rc.max_points " +
                     "FROM submission_scores ss " +
                     "JOIN rubric_criteria rc ON ss.criterion_id = rc.criterion_id " +
                     "WHERE ss.submission_id = ?";
        List<SubmissionScore> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, submissionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SubmissionScore s = new SubmissionScore();
                    s.setSubmissionId(rs.getInt("submission_id"));
                    s.setCriterionId(rs.getInt("criterion_id"));
                    s.setPoints(rs.getInt("points"));
                    s.setConceptId(rs.getInt("concept_id"));
                    s.setCriterionDescription(rs.getString("criterion_description"));
                    s.setMaxPoints(rs.getInt("max_points"));
                    list.add(s);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find scores for submission: " + submissionId, e);
        }
    }
}
