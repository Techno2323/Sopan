package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.NotificationDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcNotificationDao extends BaseJdbcDao implements NotificationDao {

    public JdbcNotificationDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Notification> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find notification by id");
    }

    @Override
    public Optional<Notification> findById(Connection conn, Integer id) {
        String sql = "SELECT notification_id, user_id, type, message, link, dedupe_key, is_read, created_at " +
                     "FROM notifications WHERE notification_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapNotification(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find notification: " + id, e);
        }
    }

    @Override
    public List<Notification> findByUserId(int userId, int limit) {
        return execute(conn -> findByUserId(conn, userId, limit), "Failed to find user notifications");
    }

    @Override
    public List<Notification> findByUserId(Connection conn, int userId, int limit) {
        String sql = "SELECT notification_id, user_id, type, message, link, dedupe_key, is_read, created_at " +
                     "FROM notifications WHERE user_id = ? ORDER BY is_read ASC, created_at DESC LIMIT ?";
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapNotification(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find notifications for user: " + userId, e);
        }
    }

    @Override
    public int countUnreadByUserId(int userId) {
        return execute(conn -> countUnreadByUserId(conn, userId), "Failed to count unread notifications");
    }

    @Override
    public int countUnreadByUserId(Connection conn, int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to count unread notifications for user: " + userId, e);
        }
    }

    @Override
    public void markAsRead(int notificationId, int userId) {
        executeVoid(conn -> markAsRead(conn, notificationId, userId), "Failed to mark notification read");
    }

    @Override
    public void markAsRead(Connection conn, int notificationId, int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to mark notification as read: " + notificationId, e);
        }
    }

    @Override
    public void markAllAsRead(int userId) {
        executeVoid(conn -> markAllAsRead(conn, userId), "Failed to mark all notifications read");
    }

    @Override
    public void markAllAsRead(Connection conn, int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to mark all notifications as read for user: " + userId, e);
        }
    }

    @Override
    public boolean saveIfNotExists(Connection conn, Notification notification) {
        String sql = "INSERT IGNORE INTO notifications (user_id, type, message, link, dedupe_key, is_read) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getType());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getLink());
            stmt.setString(5, notification.getDedupeKey());
            stmt.setBoolean(6, notification.isRead());
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DaoException("Failed to insert notification", e);
        }
    }

    @Override
    public List<Notification> findAll() {
        return execute(this::findAll, "Failed to find all notifications");
    }

    @Override
    public List<Notification> findAll(Connection conn) {
        String sql = "SELECT notification_id, user_id, type, message, link, dedupe_key, is_read, created_at " +
                     "FROM notifications ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapNotification(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list notifications", e);
        }
    }

    @Override
    public Integer save(Notification notification) {
        return execute(conn -> save(conn, notification), "Failed to save notification");
    }

    @Override
    public Integer save(Connection conn, Notification notification) {
        String sql = "INSERT INTO notifications (user_id, type, message, link, dedupe_key, is_read) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getType());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getLink());
            stmt.setString(5, notification.getDedupeKey());
            stmt.setBoolean(6, notification.isRead());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    notification.setNotificationId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain notification ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert notification", e);
        }
    }

    @Override
    public void update(Notification notification) {
        executeVoid(conn -> update(conn, notification), "Failed to update notification");
    }

    @Override
    public void update(Connection conn, Notification notification) {
        String sql = "UPDATE notifications SET is_read = ? WHERE notification_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, notification.isRead());
            stmt.setInt(2, notification.getNotificationId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update notification: " + notification.getNotificationId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete notification");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete notification: " + id, e);
        }
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setNotificationId(rs.getInt("notification_id"));
        n.setUserId(rs.getInt("user_id"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setLink(rs.getString("link"));
        n.setDedupeKey(rs.getString("dedupe_key"));
        n.setRead(rs.getBoolean("is_read"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            n.setCreatedAt(ts.toLocalDateTime());
        }
        return n;
    }
}
