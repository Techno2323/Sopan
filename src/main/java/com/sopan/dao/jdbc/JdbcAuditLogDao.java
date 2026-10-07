package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.AuditLogDao;
import com.sopan.exception.DaoException;
import com.sopan.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAuditLogDao extends BaseJdbcDao implements AuditLogDao {

    public JdbcAuditLogDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public long log(Integer userId, String action, String entity, Integer entityId) {
        return execute(conn -> log(conn, userId, action, entity, entityId), "Failed to write audit log");
    }

    @Override
    public long log(Connection conn, Integer userId, String action, String entity, Integer entityId) {
        String sql = "INSERT INTO audit_log (user_id, action, entity, entity_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, entity);
            if (entityId != null) {
                stmt.setInt(4, entityId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new DaoException("Failed to obtain audit log ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert audit log entry", e);
        }
    }

    @Override
    public List<AuditLog> findRecent(int limit) {
        return execute(conn -> findRecent(conn, limit), "Failed to find audit logs");
    }

    @Override
    public List<AuditLog> findRecent(Connection conn, int limit) {
        String sql = "SELECT al.log_id, al.user_id, al.action, al.entity, al.entity_id, al.created_at, " +
                     "       u.email AS user_email " +
                     "FROM audit_log al " +
                     "LEFT JOIN users u ON al.user_id = u.user_id " +
                     "ORDER BY al.created_at DESC, al.log_id DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setLogId(rs.getLong("log_id"));
                    int uid = rs.getInt("user_id");
                    log.setUserId(rs.wasNull() ? null : uid);
                    log.setAction(rs.getString("action"));
                    log.setEntity(rs.getString("entity"));
                    int eid = rs.getInt("entity_id");
                    log.setEntityId(rs.wasNull() ? null : eid);
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        log.setCreatedAt(ts.toLocalDateTime());
                    }
                    log.setUserEmail(rs.getString("user_email"));
                    list.add(log);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to query audit logs", e);
        }
    }
}
