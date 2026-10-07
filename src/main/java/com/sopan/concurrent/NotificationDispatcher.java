package com.sopan.concurrent;

import com.sopan.dao.NotificationDao;
import com.sopan.model.Notification;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker thread for background, non-blocking notification dispatching.
 */
public class NotificationDispatcher implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(NotificationDispatcher.class.getName());

    private final BlockingQueue<Notification> queue = new LinkedBlockingQueue<>(10_000);
    private final NotificationDao notificationDao;
    private volatile boolean running = true;

    public NotificationDispatcher(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    public boolean enqueue(Notification notification) {
        return queue.offer(notification);
    }

    @Override
    public void run() {
        while (running || !queue.isEmpty()) {
            try {
                Notification notif = queue.poll(500, TimeUnit.MILLISECONDS);
                if (notif != null) {
                    notificationDao.saveIfNotExists(null, notif);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error dispatching background notification", e);
            }
        }
    }

    public void stop() {
        this.running = false;
    }
}
