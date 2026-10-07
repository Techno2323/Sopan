package com.sopan.web.servlet;

import com.sopan.model.Notification;
import com.sopan.model.User;
import com.sopan.service.NotificationService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/learn/notifications"})
public class NotificationServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private NotificationService notificationService;

    @Override
    public void init() throws ServletException {
        this.notificationService = (NotificationService) getServletContext().getAttribute("notificationService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Notification> notifications = notificationService.getUserNotifications(user.getId(), 50);
        req.setAttribute("notifications", notifications);
        forward(req, resp, "learn/notifications");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        String action = req.getParameter("action");

        if ("markAll".equals(action)) {
            notificationService.markAllAsRead(user.getId());
            setFlashSuccess(req, "All notifications marked as read.");
        } else {
            int notifId = Validator.parseIntOrDefault(req.getParameter("id"), 0);
            if (notifId > 0) {
                notificationService.markAsRead(notifId, user.getId());
            }
        }

        redirect(req, resp, "/learn/notifications");
    }
}
