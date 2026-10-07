package com.sopan.web.servlet;

import com.sopan.dao.MaterialDao;
import com.sopan.exception.AccessDeniedException;
import com.sopan.model.Concept;
import com.sopan.model.Course;
import com.sopan.model.Material;
import com.sopan.model.User;
import com.sopan.model.enums.MaterialType;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "MaterialServlet", urlPatterns = {"/teach/materials"})
public class MaterialServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private ConceptService conceptService;
    private CourseService courseService;
    private MaterialDao materialDao;

    @Override
    public void init() throws ServletException {
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.materialDao = (MaterialDao) getServletContext().getAttribute("materialDao");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int conceptId = Validator.parseIntOrDefault(req.getParameter("concept"), 0);

        if (conceptId <= 0) {
            redirect(req, resp, "/teach/courses");
            return;
        }

        try {
            Concept concept = conceptService.getConcept(conceptId);
            Course course = courseService.getCourse(concept.getCourseId());
            courseService.checkInstructorOwnership(course, user.getId());

            List<Material> materials = materialDao.findByConceptId(conceptId);
            req.setAttribute("concept", concept);
            req.setAttribute("course", course);
            req.setAttribute("materials", materials);

            forward(req, resp, "teach/materials");

        } catch (AccessDeniedException e) {
            setFlashError(req, e.getMessage());
            redirect(req, resp, "/teach/courses");
        } catch (Exception e) {
            setFlashError(req, "Concept not found.");
            redirect(req, resp, "/teach/courses");
        }
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int conceptId = Validator.parseIntOrDefault(req.getParameter("conceptId"), 0);
        String action = req.getParameter("action");

        try {
            Concept concept = conceptService.getConcept(conceptId);
            Course course = courseService.getCourse(concept.getCourseId());
            courseService.checkInstructorOwnership(course, user.getId());

            if ("add".equals(action)) {
                String title = req.getParameter("title");
                MaterialType type = MaterialType.valueOf(req.getParameter("type"));
                String location = req.getParameter("location");

                Validator.requireNotBlank(title, "Material title");
                Validator.requireNotBlank(location, "Location or URL");

                Material m = new Material();
                m.setConceptId(conceptId);
                m.setTitle(title.trim());
                m.setType(type);
                m.setLocation(location.trim());

                materialDao.save(m);
                setFlashSuccess(req, "Material added successfully.");

            } else if ("delete".equals(action)) {
                int materialId = Validator.parseIntOrDefault(req.getParameter("materialId"), 0);
                materialDao.delete(materialId);
                setFlashSuccess(req, "Material deleted.");
            }
        } catch (Exception e) {
            setFlashError(req, "Error updating materials: " + e.getMessage());
        }

        redirect(req, resp, "/teach/materials?concept=" + conceptId);
    }
}
