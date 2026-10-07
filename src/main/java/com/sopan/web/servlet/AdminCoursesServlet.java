package com.sopan.web.servlet;

import com.sopan.model.Course;
import com.sopan.model.User;
import com.sopan.service.AdminService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminCoursesServlet", urlPatterns = {"/admin/courses"})
public class AdminCoursesServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        this.adminService = (AdminService) getServletContext().getAttribute("adminService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Course> pendingCourses = adminService.getPendingCourses();
        req.setAttribute("pendingCourses", pendingCourses);
        forward(req, resp, "admin/courses");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User admin = getCurrentUser(req);
        int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);
        String action = req.getParameter("action");

        if (courseId > 0) {
            try {
                if ("approve".equals(action)) {
                    adminService.approveCourse(admin.getId(), courseId);
                    setFlashSuccess(req, "Course approved and published to the catalog.");
                } else if ("reject".equals(action)) {
                    String reason = req.getParameter("reason");
                    adminService.rejectCourse(admin.getId(), courseId, reason);
                    setFlashSuccess(req, "Course returned to instructor as draft with feedback.");
                }
            } catch (Exception e) {
                setFlashError(req, "Error processing course review: " + e.getMessage());
            }
        }

        redirect(req, resp, "/admin/courses");
    }
}
