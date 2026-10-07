package com.sopan.web.servlet;

import com.sopan.exception.EnrollmentException;
import com.sopan.model.User;
import com.sopan.service.EnrollmentService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "EnrollServlet", urlPatterns = {"/learn/enroll"})
public class EnrollServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private EnrollmentService enrollmentService;

    @Override
    public void init() throws ServletException {
        this.enrollmentService = (EnrollmentService) getServletContext().getAttribute("enrollmentService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        redirect(req, resp, "/catalog");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);

        if (courseId <= 0) {
            setFlashError(req, "Invalid course selected.");
            redirect(req, resp, "/catalog");
            return;
        }

        try {
            enrollmentService.enroll(courseId, user.getId());
            setFlashSuccess(req, "Successfully enrolled! Explore the concept graph below.");
            redirect(req, resp, "/learn/course/map?id=" + courseId);
        } catch (EnrollmentException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/catalog/course?id=" + courseId);
        }
    }
}
