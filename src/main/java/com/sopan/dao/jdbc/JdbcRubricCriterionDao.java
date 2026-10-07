package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.RubricCriterionDao;
import com.sopan.exception.DaoException;
import com.sopan.model.RubricCriterion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRubricCriterionDao extends BaseJdbcDao implements RubricCriterionDao {

    public JdbcRubricCriterionDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<RubricCriterion> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find rubric criterion by id");
    }

    @Override
    public Optional<RubricCriterion> findById(Connection conn, Integer id) {
        String sql = "SELECT rc.criterion_id, rc.assignment_id, rc.concept_id, rc.description, rc.max_points, " +
                     "       c.title AS concept_title " +
                     "FROM rubric_criteria rc " +
                     "JOIN concepts c ON rc.concept_id = c.concept_id " +
                     "WHERE rc.criterion_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCriterion(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find rubric criterion: " + id, e);
        }
    }

    @Override
    public List<RubricCriterion> findByAssignmentId(int assignmentId) {
        return execute(conn -> findByAssignmentId(conn, assignmentId), "Failed to find assignment criteria");
    }

    @Override
    public List<RubricCriterion> findByAssignmentId(Connection conn, int assignmentId) {
        String sql = "SELECT rc.criterion_id, rc.assignment_id, rc.concept_id, rc.description, rc.max_points, " +
                     "       c.title AS concept_title " +
                     "FROM rubric_criteria rc " +
                     "JOIN concepts c ON rc.concept_id = c.concept_id " +
                     "WHERE rc.assignment_id = ? ORDER BY rc.criterion_id ASC";
        List<RubricCriterion> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCriterion(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find criteria for assignment: " + assignmentId, e);
        }
    }

    @Override
    public List<RubricCriterion> findAll() {
        return execute(this::findAll, "Failed to find all criteria");
    }

    @Override
    public List<RubricCriterion> findAll(Connection conn) {
        String sql = "SELECT rc.criterion_id, rc.assignment_id, rc.concept_id, rc.description, rc.max_points, " +
                     "       c.title AS concept_title " +
                     "FROM rubric_criteria rc JOIN concepts c ON rc.concept_id = c.concept_id";
        List<RubricCriterion> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapCriterion(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list rubric criteria", e);
        }
    }

    @Override
    public Integer save(RubricCriterion criterion) {
        return execute(conn -> save(conn, criterion), "Failed to save rubric criterion");
    }

    @Override
    public Integer save(Connection conn, RubricCriterion criterion) {
        String sql = "INSERT INTO rubric_criteria (assignment_id, concept_id, description, max_points) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, criterion.getAssignmentId());
            stmt.setInt(2, criterion.getConceptId());
            stmt.setString(3, criterion.getDescription());
            stmt.setInt(4, criterion.getMaxPoints());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    criterion.setCriterionId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain criterion ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert rubric criterion", e);
        }
    }

    @Override
    public void update(RubricCriterion criterion) {
        executeVoid(conn -> update(conn, criterion), "Failed to update criterion");
    }

    @Override
    public void update(Connection conn, RubricCriterion criterion) {
        String sql = "UPDATE rubric_criteria SET concept_id = ?, description = ?, max_points = ? WHERE criterion_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, criterion.getConceptId());
            stmt.setString(2, criterion.getDescription());
            stmt.setInt(3, criterion.getMaxPoints());
            stmt.setInt(4, criterion.getCriterionId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update criterion: " + criterion.getCriterionId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete criterion");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM rubric_criteria WHERE criterion_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete criterion: " + id, e);
        }
    }

    private RubricCriterion mapCriterion(ResultSet rs) throws SQLException {
        RubricCriterion rc = new RubricCriterion();
        rc.setCriterionId(rs.getInt("criterion_id"));
        rc.setAssignmentId(rs.getInt("assignment_id"));
        rc.setConceptId(rs.getInt("concept_id"));
        rc.setDescription(rs.getString("description"));
        rc.setMaxPoints(rs.getInt("max_points"));
        rc.setConceptTitle(rs.getString("concept_title"));
        return rc;
    }
}
