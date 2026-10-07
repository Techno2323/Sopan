package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.AuditLogDao;
import com.sopan.dao.ConceptDao;
import com.sopan.dao.CourseDao;
import com.sopan.engine.ConceptGraph;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.CyclicDependencyException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Concept;
import com.sopan.model.Course;
import com.sopan.util.Validator;

import java.util.*;

public class ConceptService {

    private final ConceptDao conceptDao;
    private final CourseDao courseDao;
    private final AuditLogDao auditLogDao;
    private final TransactionManager transactionManager;

    public ConceptService(ConceptDao conceptDao, CourseDao courseDao,
                          AuditLogDao auditLogDao, TransactionManager transactionManager) {
        this.conceptDao = conceptDao;
        this.courseDao = courseDao;
        this.auditLogDao = auditLogDao;
        this.transactionManager = transactionManager;
    }

    public Concept getConcept(int conceptId) {
        return conceptDao.findById(conceptId)
                .orElseThrow(() -> new IllegalArgumentException("Concept not found: " + conceptId));
    }

    public List<Concept> getCourseConcepts(int courseId) {
        return conceptDao.findByCourseId(courseId);
    }

    public ConceptGraph getConceptGraph(int courseId) {
        List<Concept> concepts = conceptDao.findByCourseId(courseId);
        Map<Integer, Set<Integer>> prereqMap = conceptDao.findPrerequisitesMap(courseId);
        try {
            return ConceptGraph.of(concepts, prereqMap);
        } catch (CyclicDependencyException e) {
            throw new IllegalStateException("Corrupt prerequisite graph for course " + courseId, e);
        }
    }

    public int createConcept(int courseId, int instructorId, String title, String summary, int displayOrder)
            throws ValidationException, AccessDeniedException {

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        Validator.requireNotBlank(title, "Concept title");

        Concept concept = new Concept();
        concept.setCourseId(courseId);
        concept.setTitle(title.trim());
        concept.setSummary(summary != null ? summary.trim() : "");
        concept.setDisplayOrder(displayOrder);

        return transactionManager.inTransaction(conn -> {
            int id = conceptDao.save(conn, concept);
            auditLogDao.log(conn, instructorId, "CONCEPT_CREATED", "CONCEPT", id);
            return id;
        });
    }

    public void updateConcept(int conceptId, int instructorId, String title, String summary, int displayOrder)
            throws ValidationException, AccessDeniedException {

        Concept concept = getConcept(conceptId);
        Course course = courseDao.findById(concept.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        Validator.requireNotBlank(title, "Concept title");

        concept.setTitle(title.trim());
        concept.setSummary(summary != null ? summary.trim() : "");
        concept.setDisplayOrder(displayOrder);

        transactionManager.inTransactionVoid(conn -> {
            conceptDao.update(conn, concept);
            auditLogDao.log(conn, instructorId, "CONCEPT_UPDATED", "CONCEPT", conceptId);
        });
    }

    public void deleteConcept(int conceptId, int instructorId) throws AccessDeniedException, ValidationException {
        Concept concept = getConcept(conceptId);
        Course course = courseDao.findById(concept.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        // Rule: A concept that has evidence cannot be deleted
        int evidenceCount = conceptDao.countEvidenceForConcept(conceptId);
        if (evidenceCount > 0) {
            throw new ValidationException("Cannot delete concept '" + concept.getTitle() +
                    "' because it has recorded student evidence (" + evidenceCount + " data points).");
        }

        transactionManager.inTransactionVoid(conn -> {
            conceptDao.delete(conn, conceptId);
            auditLogDao.log(conn, instructorId, "CONCEPT_DELETED", "CONCEPT", conceptId);
        });
    }

    /**
     * Validates that new prerequisites form a directed acyclic graph (DAG) via Kahn's algorithm
     * before persisting in a single transaction.
     */
    public void savePrerequisites(int courseId, int instructorId, Map<Integer, Set<Integer>> proposedPrereqs)
            throws AccessDeniedException, CyclicDependencyException {

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        List<Concept> concepts = conceptDao.findByCourseId(courseId);

        // Pre-validate graph cycle check using ConceptGraph.of
        ConceptGraph.of(concepts, proposedPrereqs);

        // Passed cycle check — save inside transaction
        transactionManager.inTransactionVoid(conn -> {
            conceptDao.savePrerequisites(conn, courseId, proposedPrereqs);
            auditLogDao.log(conn, instructorId, "PREREQUISITES_SAVED", "COURSE", courseId);
        });
    }
}
