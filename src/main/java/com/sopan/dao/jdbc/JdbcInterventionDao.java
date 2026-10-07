package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.InterventionDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Intervention;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcInterventionDao extends BaseJdbcDao implements InterventionDao {

    private static final String SELECT_BASE =
            "SELECT i.intervention_id, i.course_id, i.concept_id, i.student_id, i.instructor_id, " +
            "       i.type, i.note, i.mastery_before, i.created_at, i.resolved_at, i.mastery_after, " +
            "       u.full_name AS student_name, c.title AS concept_title " +
            "FROM interventions i " +
            "JOIN users u ON i.student_id = u.user_id " +
            "JOIN concepts c ON i.concept_id = c.concept_id ";

    public JdbcInterventionDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Intervention> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find intervention by id");
    }

    @Override
    public Optional<Intervention> findById(Connection conn, Integer id) {
        String sql = SELECT_BASE + "WHERE i.intervention_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapIntervention(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find intervention: " + id, e);
        }
    }

    @Override
    public List<Intervention> findByCourseId(int courseId) {
        return execute(conn -> findByCourseId(conn, courseId), "Failed to find interventions for course");
    }

    @Override
    public List<Intervention> findByCourseId(Connection conn, int courseId) {
        String sql = SELECT_BASE + "WHERE i.course_id = ? ORDER BY i.created_at DESC";
        List<Intervention> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIntervention(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find interventions for course: " + courseId, e);
        }
    }

    @Override
    public List<Intervention> findByStudentId(int studentId) {
        return execute(conn -> findByStudentId(conn, studentId), "Failed to find interventions for student");
    }

    @Override
    public List<Intervention> findByStudentId(Connection conn, int studentId) {
        String sql = SELECT_BASE + "WHERE i.student_id = ? ORDER BY i.created_at DESC";
        List<Intervention> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIntervention(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find interventions for student: " + studentId, e);
        }
    }

    @Override
    public List<Intervention> findUnresolvedByStudentAndConcept(Connection conn, int studentId, int conceptId) {
        String sql = SELECT_BASE + "WHERE i.student_id = ? AND i.concept_id = ? AND i.resolved_at IS NULL";
        List<Intervention> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIntervention(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find unresolved interventions for student " + studentId + " concept " + conceptId, e);
        }
    }

    @Override
    public void resolve(Connection conn, int interventionId, BigDecimal masteryAfter) {
        String sql = "UPDATE interventions SET resolved_at = CURRENT_TIMESTAMP, mastery_after = ? WHERE intervention_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, masteryAfter);
            stmt.setInt(2, interventionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to resolve intervention: " + interventionId, e);
        }
    }

    @Override
    public List<Intervention> findAll() {
        return execute(this::findAll, "Failed to find all interventions");
    }

    @Override
    public List<Intervention> findAll(Connection conn) {
        String sql = SELECT_BASE + "ORDER BY i.created_at DESC";
        List<Intervention> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapIntervention(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list interventions", e);
        }
    }

    @Override
    public Integer save(Intervention intervention) {
        return execute(conn -> save(conn, intervention), "Failed to save intervention");
    }

    @Override
    public Integer save(Connection conn, Intervention intervention) {
        String sql = "INSERT INTO interventions (course_id, concept_id, student_id, instructor_id, type, note, mastery_before) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, intervention.getCourseId());
            stmt.setInt(2, intervention.getConceptId());
            stmt.setInt(3, intervention.getStudentId());
            stmt.setInt(4, intervention.getInstructorId());
            stmt.setString(5, intervention.getType().name());
            stmt.setString(6, intervention.getNote());
            stmt.setBigDecimal(7, intervention.getMasteryBefore());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    intervention.setInterventionId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain intervention ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert intervention", e);
        }
    }

    @Override
    public void update(Intervention intervention) {
        executeVoid(conn -> update(conn, intervention), "Failed to update intervention");
    }

    @Override
    public void update(Connection conn, Intervention intervention) {
        String sql = "UPDATE interventions SET type = ?, note = ? WHERE intervention_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, intervention.getType().name());
            stmt.setString(2, intervention.getNote());
            stmt.setInt(3, intervention.getInterventionId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update intervention: " + intervention.getInterventionId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete intervention");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM interventions WHERE intervention_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete intervention: " + id, e);
        }
    }

    private Intervention mapIntervention(ResultSet rs) throws SQLException {
        Intervention i = new Intervention();
        i.setInterventionId(rs.getInt("intervention_id"));
        i.setCourseId(rs.getInt("course_id"));
        i.setConceptId(rs.getInt("concept_id"));
        i.setStudentId(rs.getInt("student_id"));
        i.setInstructorId(rs.getInt("instructor_id"));
        i.setType(Intervention.Type.valueOf(rs.getString("type")));
        i.setNote(rs.getString("note"));
        i.setMasteryBefore(rs.getBigDecimal("mastery_before"));
        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) {
            i.setCreatedAt(cTs.toLocalDateTime());
        }
        Timestamp rTs = rs.getTimestamp("resolved_at");
        if (rTs != null) {
            i.setResolvedAt(rTs.toLocalDateTime());
        }
        i.setMasteryAfter(rs.getBigDecimal("mastery_after"));
        i.setStudentName(rs.getString("student_name"));
        i.setConceptTitle(rs.getString("concept_title"));
        return i;
    }
}
