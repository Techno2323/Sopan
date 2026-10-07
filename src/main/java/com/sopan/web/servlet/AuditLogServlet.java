package com.sopan.web.servlet;

import com.sopan.model.AuditLog;
import com.sopan.service.AdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AuditLogServlet", urlPatterns = {"/admin/audit"})
public class AuditLogServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        this.adminService = (AdminService) getServletContext().getAttribute("adminService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<AuditLog> logs = adminService.getAuditLogs(100);
        req.setAttribute("logs", logs);
        forward(req, resp, "admin/audit");
    }
}
