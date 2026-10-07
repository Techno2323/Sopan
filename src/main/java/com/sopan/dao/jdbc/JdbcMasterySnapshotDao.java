package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.MasterySnapshotDao;
import com.sopan.exception.DaoException;
import com.sopan.model.MasterySnapshot;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcMasterySnapshotDao extends BaseJdbcDao implements MasterySnapshotDao {

    public JdbcMasterySnapshotDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public long save(Connection conn, MasterySnapshot snapshot) {
        String sql = "INSERT INTO mastery_snapshots (student_id, concept_id, mastery, evidence_count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, snapshot.getStudentId());
            stmt.setInt(2, snapshot.getConceptId());
            stmt.setBigDecimal(3, snapshot.getMastery());
            stmt.setInt(4, snapshot.getEvidenceCount());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    snapshot.setSnapshotId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain snapshot ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert mastery snapshot", e);
        }
    }

    @Override
    public Optional<MasterySnapshot> findLatest(int studentId, int conceptId) {
        return execute(conn -> findLatest(conn, studentId, conceptId), "Failed to find latest mastery snapshot");
    }

    @Override
    public Optional<MasterySnapshot> findLatest(Connection conn, int studentId, int conceptId) {
        String sql = "SELECT snapshot_id, student_id, concept_id, mastery, evidence_count, computed_at " +
                     "FROM mastery_snapshots " +
                     "WHERE student_id = ? AND concept_id = ? " +
                     "ORDER BY snapshot_id DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSnapshot(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find latest snapshot for student " + studentId + " and concept " + conceptId, e);
        }
    }

    @Override
    public Map<Integer, MasterySnapshot> findLatestForCourse(int studentId, int courseId) {
        return execute(conn -> findLatestForCourse(conn, studentId, courseId), "Failed to find course mastery snapshots");
    }

    @Override
    public Map<Integer, MasterySnapshot> findLatestForCourse(Connection conn, int studentId, int courseId) {
        String sql = "SELECT ms.snapshot_id, ms.student_id, ms.concept_id, ms.mastery, ms.evidence_count, ms.computed_at " +
                     "FROM mastery_snapshots ms " +
                     "JOIN ( " +
                     "    SELECT concept_id, MAX(snapshot_id) AS max_id " +
                     "    FROM mastery_snapshots " +
                     "    WHERE student_id = ? " +
                     "    GROUP BY concept_id " +
                     ") latest ON ms.snapshot_id = latest.max_id " +
                     "JOIN concepts c ON ms.concept_id = c.concept_id " +
                     "WHERE c.course_id = ?";
        Map<Integer, MasterySnapshot> map = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MasterySnapshot s = mapSnapshot(rs);
                    map.put(s.getConceptId(), s);
                }
                return map;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find latest snapshots for course: " + courseId, e);
        }
    }

    @Override
    public List<MasterySnapshot> findHistory(int studentId, int conceptId) {
        return execute(conn -> findHistory(conn, studentId, conceptId), "Failed to find mastery history");
    }

    @Override
    public List<MasterySnapshot> findHistory(Connection conn, int studentId, int conceptId) {
        String sql = "SELECT snapshot_id, student_id, concept_id, mastery, evidence_count, computed_at " +
                     "FROM mastery_snapshots " +
                     "WHERE student_id = ? AND concept_id = ? " +
                     "ORDER BY computed_at ASC, snapshot_id ASC";
        List<MasterySnapshot> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSnapshot(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find history for student: " + studentId + " concept: " + conceptId, e);
        }
    }

    private MasterySnapshot mapSnapshot(ResultSet rs) throws SQLException {
        MasterySnapshot s = new MasterySnapshot();
        s.setSnapshotId(rs.getLong("snapshot_id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setConceptId(rs.getInt("concept_id"));
        s.setMastery(rs.getBigDecimal("mastery"));
        s.setEvidenceCount(rs.getInt("evidence_count"));
        Timestamp ts = rs.getTimestamp("computed_at");
        if (ts != null) {
            s.setComputedAt(ts.toLocalDateTime());
        }
        return s;
    }
}
