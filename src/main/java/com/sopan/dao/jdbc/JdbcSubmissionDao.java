package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.SubmissionDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSubmissionDao extends BaseJdbcDao implements SubmissionDao {

    private static final String SELECT_BASE =
            "SELECT s.submission_id, s.assignment_id, s.student_id, s.body_text, s.file_path, " +
            "       s.submitted_at, s.is_late, s.status, s.graded_at, s.feedback, " +
            "       u.full_name AS student_name, a.title AS assignment_title " +
            "FROM submissions s " +
            "JOIN users u ON s.student_id = u.user_id " +
            "JOIN assignments a ON s.assignment_id = a.assignment_id ";

    public JdbcSubmissionDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Submission> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find submission by id");
    }

    @Override
    public Optional<Submission> findById(Connection conn, Integer id) {
        String sql = SELECT_BASE + "WHERE s.submission_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSubmission(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find submission: " + id, e);
        }
    }

    @Override
    public Optional<Submission> findByAssignmentAndStudent(int assignmentId, int studentId) {
        return execute(conn -> findByAssignmentAndStudent(conn, assignmentId, studentId), "Failed to find submission");
    }

    @Override
    public Optional<Submission> findByAssignmentAndStudent(Connection conn, int assignmentId, int studentId) {
        String sql = SELECT_BASE + "WHERE s.assignment_id = ? AND s.student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSubmission(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find submission for assignment: " + assignmentId + " and student: " + studentId, e);
        }
    }

    @Override
    public List<Submission> findByAssignmentId(int assignmentId) {
        return execute(conn -> findByAssignmentId(conn, assignmentId), "Failed to find submissions for assignment");
    }

    @Override
    public List<Submission> findByAssignmentId(Connection conn, int assignmentId) {
        String sql = SELECT_BASE + "WHERE s.assignment_id = ? ORDER BY s.submitted_at ASC";
        List<Submission> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubmission(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find submissions for assignment: " + assignmentId, e);
        }
    }

    @Override
    public List<Submission> findPendingGradingByInstructor(int instructorId) {
        return execute(conn -> findPendingGradingByInstructor(conn, instructorId), "Failed to find pending submissions");
    }

    @Override
    public List<Submission> findPendingGradingByInstructor(Connection conn, int instructorId) {
        String sql = SELECT_BASE +
                     "JOIN courses c ON a.course_id = c.course_id " +
                     "WHERE c.instructor_id = ? AND s.status = 'SUBMITTED' " +
                     "ORDER BY s.submitted_at ASC";
        List<Submission> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubmission(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find pending grading for instructor: " + instructorId, e);
        }
    }

    @Override
    public List<Submission> findAll() {
        return execute(this::findAll, "Failed to find all submissions");
    }

    @Override
    public List<Submission> findAll(Connection conn) {
        String sql = SELECT_BASE + "ORDER BY s.submitted_at DESC";
        List<Submission> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapSubmission(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list submissions", e);
        }
    }

    @Override
    public Integer save(Submission submission) {
        return execute(conn -> save(conn, submission), "Failed to save submission");
    }

    @Override
    public Integer save(Connection conn, Submission submission) {
        String sql = "INSERT INTO submissions (assignment_id, student_id, body_text, file_path, is_late, status, feedback) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, submission.getAssignmentId());
            stmt.setInt(2, submission.getStudentId());
            stmt.setString(3, submission.getBodyText());
            stmt.setString(4, submission.getFilePath());
            stmt.setBoolean(5, submission.isLate());
            stmt.setString(6, submission.getStatus() != null ? submission.getStatus() : "SUBMITTED");
            stmt.setString(7, submission.getFeedback());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    submission.setSubmissionId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain submission ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert submission", e);
        }
    }

    @Override
    public void update(Submission submission) {
        executeVoid(conn -> update(conn, submission), "Failed to update submission");
    }

    @Override
    public void update(Connection conn, Submission submission) {
        String sql = "UPDATE submissions SET body_text = ?, file_path = ?, is_late = ?, status = ?, graded_at = ?, feedback = ? " +
                     "WHERE submission_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, submission.getBodyText());
            stmt.setString(2, submission.getFilePath());
            stmt.setBoolean(3, submission.isLate());
            stmt.setString(4, submission.getStatus());
            if (submission.getGradedAt() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(submission.getGradedAt()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }
            stmt.setString(6, submission.getFeedback());
            stmt.setInt(7, submission.getSubmissionId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update submission: " + submission.getSubmissionId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete submission");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM submissions WHERE submission_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete submission: " + id, e);
        }
    }

    private Submission mapSubmission(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setSubmissionId(rs.getInt("submission_id"));
        s.setAssignmentId(rs.getInt("assignment_id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setBodyText(rs.getString("body_text"));
        s.setFilePath(rs.getString("file_path"));
        Timestamp subTs = rs.getTimestamp("submitted_at");
        if (subTs != null) {
            s.setSubmittedAt(subTs.toLocalDateTime());
        }
        s.setLate(rs.getBoolean("is_late"));
        s.setStatus(rs.getString("status"));
        Timestamp gradTs = rs.getTimestamp("graded_at");
        if (gradTs != null) {
            s.setGradedAt(gradTs.toLocalDateTime());
        }
        s.setFeedback(rs.getString("feedback"));
        s.setStudentName(rs.getString("student_name"));
        s.setAssignmentTitle(rs.getString("assignment_title"));
        return s;
    }
}
