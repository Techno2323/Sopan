package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.OptionDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Option;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcOptionDao extends BaseJdbcDao implements OptionDao {

    public JdbcOptionDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Option> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find option by id");
    }

    @Override
    public Optional<Option> findById(Connection conn, Integer id) {
        String sql = "SELECT option_id, question_id, label, is_correct FROM question_options WHERE option_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapOption(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find option: " + id, e);
        }
    }

    @Override
    public List<Option> findByQuestionId(int questionId) {
        return execute(conn -> findByQuestionId(conn, questionId), "Failed to find question options");
    }

    @Override
    public List<Option> findByQuestionId(Connection conn, int questionId) {
        String sql = "SELECT option_id, question_id, label, is_correct FROM question_options WHERE question_id = ? ORDER BY option_id ASC";
        List<Option> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOption(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find options for question: " + questionId, e);
        }
    }

    @Override
    public List<Option> findAll() {
        return execute(this::findAll, "Failed to find all options");
    }

    @Override
    public List<Option> findAll(Connection conn) {
        String sql = "SELECT option_id, question_id, label, is_correct FROM question_options";
        List<Option> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapOption(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list options", e);
        }
    }

    @Override
    public Integer save(Option option) {
        return execute(conn -> save(conn, option), "Failed to save option");
    }

    @Override
    public Integer save(Connection conn, Option option) {
        String sql = "INSERT INTO question_options (question_id, label, is_correct) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, option.getQuestionId());
            stmt.setString(2, option.getLabel());
            stmt.setBoolean(3, option.isCorrect());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    option.setOptionId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain option ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert option", e);
        }
    }

    @Override
    public void update(Option option) {
        executeVoid(conn -> update(conn, option), "Failed to update option");
    }

    @Override
    public void update(Connection conn, Option option) {
        String sql = "UPDATE question_options SET label = ?, is_correct = ? WHERE option_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, option.getLabel());
            stmt.setBoolean(2, option.isCorrect());
            stmt.setInt(3, option.getOptionId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update option: " + option.getOptionId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete option");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM question_options WHERE option_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete option: " + id, e);
        }
    }

    private Option mapOption(ResultSet rs) throws SQLException {
        Option o = new Option();
        o.setOptionId(rs.getInt("option_id"));
        o.setQuestionId(rs.getInt("question_id"));
        o.setLabel(rs.getString("label"));
        o.setCorrect(rs.getBoolean("is_correct"));
        return o;
    }
}
