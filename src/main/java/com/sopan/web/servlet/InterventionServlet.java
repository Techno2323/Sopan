package com.sopan.web.servlet;

import com.sopan.dao.EnrollmentDao;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Concept;
import com.sopan.model.Course;
import com.sopan.model.Enrollment;
import com.sopan.model.Intervention;
import com.sopan.model.User;
import com.sopan.service.ConceptService;
import com.sopan.service.CourseService;
import com.sopan.service.InterventionService;
import com.sopan.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "InterventionServlet", urlPatterns = {"/teach/interventions"})
public class InterventionServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;


    private CourseService courseService;
    private ConceptService conceptService;
    private EnrollmentDao enrollmentDao;
    private InterventionService interventionService;

    @Override
    public void init() throws ServletException {
        this.courseService = (CourseService) getServletContext().getAttribute("courseService");
        this.conceptService = (ConceptService) getServletContext().getAttribute("conceptService");
        this.enrollmentDao = (EnrollmentDao) getServletContext().getAttribute("enrollmentDao");
        this.interventionService = (InterventionService) getServletContext().getAttribute("interventionService");
    }

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        List<Course> courses = courseService.getInstructorCourses(user.getId());

        int selectedCourseId = Validator.parseIntOrDefault(req.getParameter("course"), 0);
        if (selectedCourseId <= 0 && !courses.isEmpty()) {
            selectedCourseId = courses.get(0).getCourseId();
        }

        Course selectedCourse = null;
        List<Intervention> interventions = new ArrayList<>();
        List<Concept> concepts = new ArrayList<>();
        List<Enrollment> enrolledStudents = new ArrayList<>();

        if (selectedCourseId > 0) {
            final int cId = selectedCourseId;
            selectedCourse = courses.stream().filter(c -> c.getCourseId() == cId).findFirst().orElse(null);
            if (selectedCourse != null) {
                concepts = conceptService.getCourseConcepts(selectedCourseId);
                enrolledStudents = enrollmentDao.findByCourseId(selectedCourseId);
                try {
                    interventions = interventionService.getCourseInterventions(selectedCourseId, user.getId());
                } catch (Exception e) {
                    setFlashError(req, "Error loading interventions: " + e.getMessage());
                }
            }
        }

        req.setAttribute("courses", courses);
        req.setAttribute("selectedCourse", selectedCourse);
        req.setAttribute("concepts", concepts);
        req.setAttribute("enrolledStudents", enrolledStudents);
        req.setAttribute("interventions", interventions);

        forward(req, resp, "teach/interventions");
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        int courseId = Validator.parseIntOrDefault(req.getParameter("courseId"), 0);

        try {
            int studentId = Validator.parseIntOrDefault(req.getParameter("studentId"), 0);
            int conceptId = Validator.parseIntOrDefault(req.getParameter("conceptId"), 0);
            Intervention.Type type = Intervention.Type.valueOf(req.getParameter("type"));
            String note = req.getParameter("note");

            interventionService.logIntervention(courseId, user.getId(), studentId, conceptId, type, note);
            setFlashSuccess(req, "Academic intervention logged with initial mastery snapshot.");

        } catch (ValidationException | AccessDeniedException e) {
            setFlashError(req, e.getMessage());
        } catch (Exception e) {
            setFlashError(req, "Error saving intervention: " + e.getMessage());
        }

        redirect(req, resp, "/teach/interventions?course=" + courseId);
    }
}
