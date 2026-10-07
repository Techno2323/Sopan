package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.AttemptAnswerDao;
import com.sopan.exception.DaoException;
import com.sopan.model.AttemptAnswer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcAttemptAnswerDao extends BaseJdbcDao implements AttemptAnswerDao {

    public JdbcAttemptAnswerDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public void saveAll(Connection conn, List<AttemptAnswer> answers) {
        if (answers == null || answers.isEmpty()) return;
        String sql = "INSERT INTO attempt_answers (attempt_id, question_id, selected_option_id, is_correct) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (AttemptAnswer ans : answers) {
                stmt.setInt(1, ans.getAttemptId());
                stmt.setInt(2, ans.getQuestionId());
                if (ans.getSelectedOptionId() != null) {
                    stmt.setInt(3, ans.getSelectedOptionId());
                } else {
                    stmt.setNull(3, java.sql.Types.INTEGER);
                }
                stmt.setBoolean(4, ans.isCorrect());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DaoException("Failed to save attempt answers", e);
        }
    }

    @Override
    public List<AttemptAnswer> findByAttemptId(int attemptId) {
        return execute(conn -> findByAttemptId(conn, attemptId), "Failed to find attempt answers");
    }

    @Override
    public List<AttemptAnswer> findByAttemptId(Connection conn, int attemptId) {
        String sql = "SELECT attempt_id, question_id, selected_option_id, is_correct FROM attempt_answers WHERE attempt_id = ?";
        List<AttemptAnswer> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, attemptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AttemptAnswer ans = new AttemptAnswer();
                    ans.setAttemptId(rs.getInt("attempt_id"));
                    ans.setQuestionId(rs.getInt("question_id"));
                    int optId = rs.getInt("selected_option_id");
                    ans.setSelectedOptionId(rs.wasNull() ? null : optId);
                    ans.setCorrect(rs.getBoolean("is_correct"));
                    list.add(ans);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find answers for attempt: " + attemptId, e);
        }
    }
}
