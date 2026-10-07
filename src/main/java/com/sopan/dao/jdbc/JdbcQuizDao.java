package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.QuizDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcQuizDao extends BaseJdbcDao implements QuizDao {

    public JdbcQuizDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Quiz> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find quiz by id");
    }

    @Override
    public Optional<Quiz> findById(Connection conn, Integer id) {
        String sql = "SELECT quiz_id, course_id, title, max_attempts, status FROM quizzes WHERE quiz_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapQuiz(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find quiz: " + id, e);
        }
    }

    @Override
    public List<Quiz> findByCourseId(int courseId) {
        return execute(conn -> findByCourseId(conn, courseId), "Failed to find quizzes for course");
    }

    @Override
    public List<Quiz> findByCourseId(Connection conn, int courseId) {
        String sql = "SELECT quiz_id, course_id, title, max_attempts, status FROM quizzes WHERE course_id = ? ORDER BY quiz_id ASC";
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapQuiz(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find quizzes for course: " + courseId, e);
        }
    }

    @Override
    public List<Quiz> findPublishedByCourseId(int courseId) {
        return execute(conn -> findPublishedByCourseId(conn, courseId), "Failed to find published quizzes");
    }

    @Override
    public List<Quiz> findPublishedByCourseId(Connection conn, int courseId) {
        String sql = "SELECT quiz_id, course_id, title, max_attempts, status FROM quizzes WHERE course_id = ? AND status = 'PUBLISHED' ORDER BY quiz_id ASC";
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapQuiz(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find published quizzes for course: " + courseId, e);
        }
    }

    @Override
    public List<Quiz> findAll() {
        return execute(this::findAll, "Failed to find all quizzes");
    }

    @Override
    public List<Quiz> findAll(Connection conn) {
        String sql = "SELECT quiz_id, course_id, title, max_attempts, status FROM quizzes ORDER BY quiz_id DESC";
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapQuiz(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list quizzes", e);
        }
    }

    @Override
    public Integer save(Quiz quiz) {
        return execute(conn -> save(conn, quiz), "Failed to save quiz");
    }

    @Override
    public Integer save(Connection conn, Quiz quiz) {
        String sql = "INSERT INTO quizzes (course_id, title, max_attempts, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, quiz.getCourseId());
            stmt.setString(2, quiz.getTitle());
            stmt.setInt(3, quiz.getMaxAttempts());
            stmt.setString(4, quiz.getStatus() != null ? quiz.getStatus() : "DRAFT");
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    quiz.setQuizId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain quiz ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert quiz: " + quiz.getTitle(), e);
        }
    }

    @Override
    public void update(Quiz quiz) {
        executeVoid(conn -> update(conn, quiz), "Failed to update quiz");
    }

    @Override
    public void update(Connection conn, Quiz quiz) {
        String sql = "UPDATE quizzes SET title = ?, max_attempts = ?, status = ? WHERE quiz_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, quiz.getTitle());
            stmt.setInt(2, quiz.getMaxAttempts());
            stmt.setString(3, quiz.getStatus());
            stmt.setInt(4, quiz.getQuizId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update quiz: " + quiz.getQuizId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete quiz");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM quizzes WHERE quiz_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete quiz: " + id, e);
        }
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setQuizId(rs.getInt("quiz_id"));
        q.setCourseId(rs.getInt("course_id"));
        q.setTitle(rs.getString("title"));
        q.setMaxAttempts(rs.getInt("max_attempts"));
        q.setStatus(rs.getString("status"));
        return q;
    }
}
