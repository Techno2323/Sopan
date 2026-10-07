package com.sopan.web.servlet;

import com.sopan.model.User;
import com.sopan.model.enums.AccountStatus;
import com.sopan.service.AdminService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminUsersServlet", urlPatterns = {"/admin/users"})
public class AdminUsersServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        this.adminService = (AdminService) getServletContext().getAttribute("adminService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = adminService.getAllUsers();
        req.setAttribute("users", users);
        forward(req, resp, "admin/users");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User admin = getCurrentUser(req);
        int targetUserId = Validator.parseIntOrDefault(req.getParameter("userId"), 0);
        String newStatusStr = req.getParameter("status");

        if (targetUserId > 0 && newStatusStr != null) {
            try {
                AccountStatus newStatus = AccountStatus.valueOf(newStatusStr);
                adminService.updateUserStatus(admin.getId(), targetUserId, newStatus);
                setFlashSuccess(req, "User status updated to " + newStatus.name());
            } catch (Exception e) {
                setFlashError(req, "Error updating status: " + e.getMessage());
            }
        }

        redirect(req, resp, "/admin/users");
    }
}
