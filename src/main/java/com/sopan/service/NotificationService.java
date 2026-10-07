package com.sopan.service;

import com.sopan.dao.NotificationDao;
import com.sopan.model.Notification;

import java.util.List;

public class NotificationService {

    private final NotificationDao notificationDao;

    public NotificationService(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public List<Notification> getUserNotifications(int userId, int limit) {
        return notificationDao.findByUserId(userId, limit);
    }

    public int getUnreadCount(int userId) {
        return notificationDao.countUnreadByUserId(userId);
    }

    public void markAsRead(int notificationId, int userId) {
        notificationDao.markAsRead(notificationId, userId);
    }

    public void markAllAsRead(int userId) {
        notificationDao.markAllAsRead(userId);
    }

    public boolean createNotification(int userId, String type, String message, String link, String dedupeKey) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type);
        notif.setMessage(message);
        notif.setLink(link);
        notif.setDedupeKey(dedupeKey);
        notif.setRead(false);
        return notificationDao.saveIfNotExists(null, notif);
    }
}
