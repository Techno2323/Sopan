package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.QuestionDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcQuestionDao extends BaseJdbcDao implements QuestionDao {

    public JdbcQuestionDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Question> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find question by id");
    }

    @Override
    public Optional<Question> findById(Connection conn, Integer id) {
        String sql = "SELECT q.question_id, q.quiz_id, q.concept_id, q.prompt, q.marks, c.title AS concept_title " +
                     "FROM quiz_questions q " +
                     "JOIN concepts c ON q.concept_id = c.concept_id " +
                     "WHERE q.question_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapQuestion(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find question: " + id, e);
        }
    }

    @Override
    public List<Question> findByQuizId(int quizId) {
        return execute(conn -> findByQuizId(conn, quizId), "Failed to find quiz questions");
    }

    @Override
    public List<Question> findByQuizId(Connection conn, int quizId) {
        String sql = "SELECT q.question_id, q.quiz_id, q.concept_id, q.prompt, q.marks, c.title AS concept_title " +
                     "FROM quiz_questions q " +
                     "JOIN concepts c ON q.concept_id = c.concept_id " +
                     "WHERE q.quiz_id = ? ORDER BY q.question_id ASC";
        List<Question> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapQuestion(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find questions for quiz: " + quizId, e);
        }
    }

    @Override
    public List<Question> findAll() {
        return execute(this::findAll, "Failed to find all questions");
    }

    @Override
    public List<Question> findAll(Connection conn) {
        String sql = "SELECT q.question_id, q.quiz_id, q.concept_id, q.prompt, q.marks, c.title AS concept_title " +
                     "FROM quiz_questions q JOIN concepts c ON q.concept_id = c.concept_id";
        List<Question> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapQuestion(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list questions", e);
        }
    }

    @Override
    public Integer save(Question question) {
        return execute(conn -> save(conn, question), "Failed to save question");
    }

    @Override
    public Integer save(Connection conn, Question question) {
        String sql = "INSERT INTO quiz_questions (quiz_id, concept_id, prompt, marks) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, question.getQuizId());
            stmt.setInt(2, question.getConceptId());
            stmt.setString(3, question.getPrompt());
            stmt.setInt(4, question.getMarks());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    question.setQuestionId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain question ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert question", e);
        }
    }

    @Override
    public void update(Question question) {
        executeVoid(conn -> update(conn, question), "Failed to update question");
    }

    @Override
    public void update(Connection conn, Question question) {
        String sql = "UPDATE quiz_questions SET concept_id = ?, prompt = ?, marks = ? WHERE question_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, question.getConceptId());
            stmt.setString(2, question.getPrompt());
            stmt.setInt(3, question.getMarks());
            stmt.setInt(4, question.getQuestionId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update question: " + question.getQuestionId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete question");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM quiz_questions WHERE question_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete question: " + id, e);
        }
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("question_id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setConceptId(rs.getInt("concept_id"));
        q.setPrompt(rs.getString("prompt"));
        q.setMarks(rs.getInt("marks"));
        q.setConceptTitle(rs.getString("concept_title"));
        return q;
    }
}
