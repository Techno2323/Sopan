package com.sopan.web.servlet;

import com.sopan.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "LandingServlet", urlPatterns = {""})
public class LandingServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser != null) {
            redirect(req, resp, currentUser.homePath());
            return;
        }
        forward(req, resp, "landing");
    }
}
