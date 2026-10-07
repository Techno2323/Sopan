package com.sopan.web.servlet;

import com.sopan.model.User;
import com.sopan.util.FileStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(name = "FileDownloadServlet", urlPatterns = {"/uploads/*"})
public class FileDownloadServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private FileStorage fileStorage;

    @Override
    public void init() throws ServletException {
        this.fileStorage = (FileStorage) getServletContext().getAttribute("fileStorage");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required to download files.");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filename = pathInfo.substring(1);
        try {
            Path file = fileStorage.resolve(filename);
            if (!Files.exists(file) || !Files.isRegularFile(file)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            resp.setContentLengthLong(Files.size(file));

            try (InputStream in = Files.newInputStream(file);
                 OutputStream out = resp.getOutputStream()) {
                in.transferTo(out);
            }

        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file requested.");
        }
    }
}
