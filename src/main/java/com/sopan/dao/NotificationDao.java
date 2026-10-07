package com.sopan.dao;

import com.sopan.model.Notification;

import java.sql.Connection;
import java.util.List;

public interface NotificationDao extends Dao<Notification, Integer> {

    List<Notification> findByUserId(int userId, int limit);
    List<Notification> findByUserId(Connection conn, int userId, int limit);

    int countUnreadByUserId(int userId);
    int countUnreadByUserId(Connection conn, int userId);

    void markAsRead(int notificationId, int userId);
    void markAsRead(Connection conn, int notificationId, int userId);

    void markAllAsRead(int userId);
    void markAllAsRead(Connection conn, int userId);

    boolean saveIfNotExists(Connection conn, Notification notification);
}
