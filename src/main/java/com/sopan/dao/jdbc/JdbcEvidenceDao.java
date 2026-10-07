package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.EvidenceDao;
import com.sopan.exception.DaoException;
import com.sopan.model.evidence.Evidence;
import com.sopan.model.evidence.QuizEvidence;
import com.sopan.model.evidence.RubricEvidence;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcEvidenceDao extends BaseJdbcDao implements EvidenceDao {

    public JdbcEvidenceDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public List<Evidence> findByStudentAndConcept(int studentId, int conceptId) {
        return execute(conn -> findByStudentAndConcept(conn, studentId, conceptId), "Failed to find evidence for concept");
    }

    @Override
    public List<Evidence> findByStudentAndConcept(Connection conn, int studentId, int conceptId) {
        String sql = "SELECT student_id, concept_id, score, weight, recorded_at " +
                     "FROM v_evidence " +
                     "WHERE student_id = ? AND concept_id = ? " +
                     "ORDER BY recorded_at ASC";
        List<Evidence> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEvidence(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find evidence for student " + studentId + " and concept " + conceptId, e);
        }
    }

    @Override
    public Map<Integer, List<Evidence>> findByStudentAndCourse(int studentId, int courseId) {
        return execute(conn -> findByStudentAndCourse(conn, studentId, courseId), "Failed to find course evidence");
    }

    @Override
    public Map<Integer, List<Evidence>> findByStudentAndCourse(Connection conn, int studentId, int courseId) {
        String sql = "SELECT ve.student_id, ve.concept_id, ve.score, ve.weight, ve.recorded_at " +
                     "FROM v_evidence ve " +
                     "JOIN concepts c ON ve.concept_id = c.concept_id " +
                     "WHERE ve.student_id = ? AND c.course_id = ? " +
                     "ORDER BY ve.recorded_at ASC";
        Map<Integer, List<Evidence>> map = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Evidence ev = mapEvidence(rs);
                    map.computeIfAbsent(ev.conceptId(), k -> new ArrayList<>()).add(ev);
                }
                return map;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find evidence for student " + studentId + " and course " + courseId, e);
        }
    }

    private Evidence mapEvidence(ResultSet rs) throws SQLException {
        int conceptId = rs.getInt("concept_id");
        double score = rs.getDouble("score");
        int weight = rs.getInt("weight");
        Timestamp ts = rs.getTimestamp("recorded_at");
        Instant recordedAt = ts != null ? ts.toInstant() : Instant.now();

        if (weight == 3) {
            return new RubricEvidence(conceptId, score, recordedAt);
        } else {
            return new QuizEvidence(conceptId, score, recordedAt);
        }
    }
}
