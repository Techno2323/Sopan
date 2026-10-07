package com.sopan.web.servlet;

import com.sopan.dto.DashboardView;
import com.sopan.model.User;
import com.sopan.service.RecommendationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "StudentHomeServlet", urlPatterns = {"/learn/home"})
public class StudentHomeServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private RecommendationService recommendationService;

    @Override
    public void init() throws ServletException {
        this.recommendationService = (RecommendationService) getServletContext().getAttribute("recommendationService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        DashboardView dashboard = recommendationService.getDashboard(user.getId());
        req.setAttribute("dashboard", dashboard);
        forward(req, resp, "learn/home");
    }
}
