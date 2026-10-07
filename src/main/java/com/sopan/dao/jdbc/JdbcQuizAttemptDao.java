package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.QuizAttemptDao;
import com.sopan.exception.DaoException;
import com.sopan.model.QuizAttempt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcQuizAttemptDao extends BaseJdbcDao implements QuizAttemptDao {

    public JdbcQuizAttemptDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<QuizAttempt> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find attempt by id");
    }

    @Override
    public Optional<QuizAttempt> findById(Connection conn, Integer id) {
        String sql = "SELECT attempt_id, quiz_id, student_id, attempt_no, score, max_score, submitted_at FROM quiz_attempts WHERE attempt_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAttempt(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find attempt: " + id, e);
        }
    }

    @Override
    public List<QuizAttempt> findByQuizAndStudent(int quizId, int studentId) {
        return execute(conn -> findByQuizAndStudent(conn, quizId, studentId), "Failed to find student attempts");
    }

    @Override
    public List<QuizAttempt> findByQuizAndStudent(Connection conn, int quizId, int studentId) {
        String sql = "SELECT attempt_id, quiz_id, student_id, attempt_no, score, max_score, submitted_at " +
                     "FROM quiz_attempts WHERE quiz_id = ? AND student_id = ? ORDER BY attempt_no ASC";
        List<QuizAttempt> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAttempt(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find attempts for quiz: " + quizId + " and student: " + studentId, e);
        }
    }

    @Override
    public int findLatestAttemptNo(int quizId, int studentId) {
        return execute(conn -> findLatestAttemptNo(conn, quizId, studentId), "Failed to find latest attempt number");
    }

    @Override
    public int findLatestAttemptNo(Connection conn, int quizId, int studentId) {
        String sql = "SELECT COALESCE(MAX(attempt_no), 0) FROM quiz_attempts WHERE quiz_id = ? AND student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find latest attempt number", e);
        }
    }

    @Override
    public Optional<QuizAttempt> findByQuizStudentAndAttemptNo(Connection conn, int quizId, int studentId, int attemptNo) {
        String sql = "SELECT attempt_id, quiz_id, student_id, attempt_no, score, max_score, submitted_at " +
                     "FROM quiz_attempts WHERE quiz_id = ? AND student_id = ? AND attempt_no = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, studentId);
            stmt.setInt(3, attemptNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAttempt(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find attempt by number: " + attemptNo, e);
        }
    }

    @Override
    public List<QuizAttempt> findAll() {
        return execute(this::findAll, "Failed to find all attempts");
    }

    @Override
    public List<QuizAttempt> findAll(Connection conn) {
        String sql = "SELECT attempt_id, quiz_id, student_id, attempt_no, score, max_score, submitted_at FROM quiz_attempts ORDER BY submitted_at DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapAttempt(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list attempts", e);
        }
    }

    @Override
    public Integer save(QuizAttempt attempt) {
        return execute(conn -> save(conn, attempt), "Failed to save attempt");
    }

    @Override
    public Integer save(Connection conn, QuizAttempt attempt) {
        String sql = "INSERT INTO quiz_attempts (quiz_id, student_id, attempt_no, score, max_score) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attempt.getQuizId());
            stmt.setInt(2, attempt.getStudentId());
            stmt.setInt(3, attempt.getAttemptNo());
            stmt.setInt(4, attempt.getScore());
            stmt.setInt(5, attempt.getMaxScore());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    attempt.setAttemptId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain attempt ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert attempt", e);
        }
    }

    @Override
    public void update(QuizAttempt attempt) {
        executeVoid(conn -> update(conn, attempt), "Failed to update attempt");
    }

    @Override
    public void update(Connection conn, QuizAttempt attempt) {
        String sql = "UPDATE quiz_attempts SET score = ?, max_score = ? WHERE attempt_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attempt.getScore());
            stmt.setInt(2, attempt.getMaxScore());
            stmt.setInt(3, attempt.getAttemptId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update attempt: " + attempt.getAttemptId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete attempt");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM quiz_attempts WHERE attempt_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete attempt: " + id, e);
        }
    }

    private QuizAttempt mapAttempt(ResultSet rs) throws SQLException {
        QuizAttempt a = new QuizAttempt();
        a.setAttemptId(rs.getInt("attempt_id"));
        a.setQuizId(rs.getInt("quiz_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setAttemptNo(rs.getInt("attempt_no"));
        a.setScore(rs.getInt("score"));
        a.setMaxScore(rs.getInt("max_score"));
        Timestamp ts = rs.getTimestamp("submitted_at");
        if (ts != null) {
            a.setSubmittedAt(ts.toLocalDateTime());
        }
        return a;
    }
}
