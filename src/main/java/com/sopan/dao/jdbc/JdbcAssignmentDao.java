package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.AssignmentDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAssignmentDao extends BaseJdbcDao implements AssignmentDao {

    public JdbcAssignmentDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Assignment> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find assignment by id");
    }

    @Override
    public Optional<Assignment> findById(Connection conn, Integer id) {
        String sql = "SELECT assignment_id, course_id, title, instructions, due_at, allow_late FROM assignments WHERE assignment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAssignment(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find assignment: " + id, e);
        }
    }

    @Override
    public List<Assignment> findByCourseId(int courseId) {
        return execute(conn -> findByCourseId(conn, courseId), "Failed to find course assignments");
    }

    @Override
    public List<Assignment> findByCourseId(Connection conn, int courseId) {
        String sql = "SELECT assignment_id, course_id, title, instructions, due_at, allow_late FROM assignments WHERE course_id = ? ORDER BY due_at ASC";
        List<Assignment> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAssignment(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find assignments for course: " + courseId, e);
        }
    }

    @Override
    public List<Assignment> findUpcomingByStudentId(int studentId, int limit) {
        return execute(conn -> findUpcomingByStudentId(conn, studentId, limit), "Failed to find upcoming assignments");
    }

    @Override
    public List<Assignment> findUpcomingByStudentId(Connection conn, int studentId, int limit) {
        String sql = "SELECT a.assignment_id, a.course_id, a.title, a.instructions, a.due_at, a.allow_late " +
                     "FROM assignments a " +
                     "JOIN enrollments e ON a.course_id = e.course_id " +
                     "LEFT JOIN submissions s ON a.assignment_id = s.assignment_id AND s.student_id = ? " +
                     "WHERE e.student_id = ? AND e.status = 'ACTIVE' " +
                     "  AND (s.submission_id IS NULL OR s.status = 'SUBMITTED') " +
                     "ORDER BY a.due_at ASC LIMIT ?";
        List<Assignment> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, studentId);
            stmt.setInt(3, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAssignment(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find upcoming assignments for student: " + studentId, e);
        }
    }

    @Override
    public List<Assignment> findAll() {
        return execute(this::findAll, "Failed to find all assignments");
    }

    @Override
    public List<Assignment> findAll(Connection conn) {
        String sql = "SELECT assignment_id, course_id, title, instructions, due_at, allow_late FROM assignments ORDER BY due_at DESC";
        List<Assignment> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapAssignment(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list assignments", e);
        }
    }

    @Override
    public Integer save(Assignment assignment) {
        return execute(conn -> save(conn, assignment), "Failed to save assignment");
    }

    @Override
    public Integer save(Connection conn, Assignment assignment) {
        String sql = "INSERT INTO assignments (course_id, title, instructions, due_at, allow_late) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, assignment.getCourseId());
            stmt.setString(2, assignment.getTitle());
            stmt.setString(3, assignment.getInstructions());
            stmt.setTimestamp(4, Timestamp.valueOf(assignment.getDueAt()));
            stmt.setBoolean(5, assignment.isAllowLate());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    assignment.setAssignmentId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain assignment ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert assignment: " + assignment.getTitle(), e);
        }
    }

    @Override
    public void update(Assignment assignment) {
        executeVoid(conn -> update(conn, assignment), "Failed to update assignment");
    }

    @Override
    public void update(Connection conn, Assignment assignment) {
        String sql = "UPDATE assignments SET title = ?, instructions = ?, due_at = ?, allow_late = ? WHERE assignment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, assignment.getTitle());
            stmt.setString(2, assignment.getInstructions());
            stmt.setTimestamp(3, Timestamp.valueOf(assignment.getDueAt()));
            stmt.setBoolean(4, assignment.isAllowLate());
            stmt.setInt(5, assignment.getAssignmentId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update assignment: " + assignment.getAssignmentId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete assignment");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM assignments WHERE assignment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete assignment: " + id, e);
        }
    }

    private Assignment mapAssignment(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setAssignmentId(rs.getInt("assignment_id"));
        a.setCourseId(rs.getInt("course_id"));
        a.setTitle(rs.getString("title"));
        a.setInstructions(rs.getString("instructions"));
        Timestamp ts = rs.getTimestamp("due_at");
        if (ts != null) {
            a.setDueAt(ts.toLocalDateTime());
        }
        a.setAllowLate(rs.getBoolean("allow_late"));
        return a;
    }
}
