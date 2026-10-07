package com.sopan.web.servlet;

import com.sopan.model.Course;
import com.sopan.service.CourseService;
import com.sopan.util.Page;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "CatalogServlet", urlPatterns = {"/catalog"})
public class CatalogServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("q");
        String category = req.getParameter("category");
        String difficulty = req.getParameter("difficulty");
        String sortBy = req.getParameter("sortBy");
        String sortOrder = req.getParameter("sortOrder");
        int page = Math.max(1, Validator.parseIntOrDefault(req.getParameter("page"), 1));
        int pageSize = 9;

        Page<Course> coursePage = courseService.searchPublishedCourses(
                query, category, difficulty, sortBy, sortOrder, page, pageSize
        );

        req.setAttribute("page", coursePage);
        req.setAttribute("query", query);
        req.setAttribute("category", category);
        req.setAttribute("difficulty", difficulty);
        req.setAttribute("sortBy", sortBy != null ? sortBy : "created_at");
        req.setAttribute("sortOrder", sortOrder != null ? sortOrder : "desc");

        forward(req, resp, "catalog/list");
    }
}
