package com.sopan.web.servlet;

import com.sopan.model.AuditLog;
import com.sopan.model.Course;
import com.sopan.model.User;
import com.sopan.model.enums.AccountStatus;
import com.sopan.service.AdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminHomeServlet", urlPatterns = {"/admin/home"})
public class AdminHomeServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        this.adminService = (AdminService) getServletContext().getAttribute("adminService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Course> pendingCourses = adminService.getPendingCourses();
        List<User> allUsers = adminService.getAllUsers();
        long suspendedCount = allUsers.stream().filter(u -> u.getStatus() == AccountStatus.SUSPENDED).count();
        List<AuditLog> recentLogs = adminService.getAuditLogs(10);

        req.setAttribute("pendingCourses", pendingCourses);
        req.setAttribute("totalUsers", allUsers.size());
        req.setAttribute("suspendedCount", suspendedCount);
        req.setAttribute("recentLogs", recentLogs);

        forward(req, resp, "admin/home");
    }
}
