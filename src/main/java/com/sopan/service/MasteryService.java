package com.sopan.service;

import com.sopan.dao.ConceptDao;
import com.sopan.dao.CourseDao;
import com.sopan.dao.EvidenceDao;
import com.sopan.dao.MasterySnapshotDao;
import com.sopan.dto.ConceptStatusView;
import com.sopan.engine.MasteryPolicy;
import com.sopan.engine.RecencyWeightedPolicy;
import com.sopan.exception.PrerequisiteNotMetException;
import com.sopan.model.Concept;
import com.sopan.model.Course;
import com.sopan.model.MasterySnapshot;
import com.sopan.model.enums.MasteryLevel;
import com.sopan.model.evidence.Evidence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.time.Instant;
import java.util.*;

public class MasteryService {

    private final EvidenceDao evidenceDao;
    private final MasterySnapshotDao masterySnapshotDao;
    private final ConceptDao conceptDao;
    private final CourseDao courseDao;
    private final MasteryPolicy masteryPolicy;

    public MasteryService(EvidenceDao evidenceDao, MasterySnapshotDao masterySnapshotDao,
                          ConceptDao conceptDao, CourseDao courseDao) {
        this(evidenceDao, masterySnapshotDao, conceptDao, courseDao, new RecencyWeightedPolicy());
    }

    public MasteryService(EvidenceDao evidenceDao, MasterySnapshotDao masterySnapshotDao,
                          ConceptDao conceptDao, CourseDao courseDao, MasteryPolicy masteryPolicy) {
        this.evidenceDao = evidenceDao;
        this.masterySnapshotDao = masterySnapshotDao;
        this.conceptDao = conceptDao;
        this.courseDao = courseDao;
        this.masteryPolicy = masteryPolicy;
    }

    /**
     * Recomputes continuous mastery score from all evidence rows in v_evidence
     * and appends a new mastery_snapshots record within the caller's transaction.
     */
    public MasterySnapshot recomputeAndSaveSnapshot(Connection conn, int studentId, int conceptId) {
        List<Evidence> evidence = evidenceDao.findByStudentAndConcept(conn, studentId, conceptId);
        int evidenceCount = evidence.size();

        double score = masteryPolicy.mastery(evidence, Instant.now());
        BigDecimal masteryValue = BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);

        MasterySnapshot snapshot = new MasterySnapshot();
        snapshot.setStudentId(studentId);
        snapshot.setConceptId(conceptId);
        snapshot.setMastery(masteryValue);
        snapshot.setEvidenceCount(evidenceCount);

        masterySnapshotDao.save(conn, snapshot);
        return snapshot;
    }

    public void recomputeForConcepts(Connection conn, int studentId, Set<Integer> conceptIds) {
        if (conceptIds == null) return;
        for (int cId : conceptIds) {
            recomputeAndSaveSnapshot(conn, studentId, cId);
        }
    }

    public Map<Integer, Double> getMasteryMap(int studentId, int courseId) {
        Map<Integer, MasterySnapshot> latestMap = masterySnapshotDao.findLatestForCourse(studentId, courseId);
        Map<Integer, Double> map = new HashMap<>();
        for (Map.Entry<Integer, MasterySnapshot> entry : latestMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getMastery().doubleValue());
        }
        return map;
    }

    /**
     * Verifies that all prerequisites for a concept have been satisfied by the student
     * based on the course gate threshold. Throws PrerequisiteNotMetException if locked.
     */
    public void checkPrerequisitesMet(int studentId, int courseId, int conceptId)
            throws PrerequisiteNotMetException {

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        double gate = course.getGateThreshold();

        Set<Integer> prereqIds = conceptDao.findPrerequisitesForConcept(conceptId);
        if (prereqIds.isEmpty()) {
            return;
        }

        for (int pId : prereqIds) {
            Optional<MasterySnapshot> snapOpt = masterySnapshotDao.findLatest(studentId, pId);
            double currentMastery = snapOpt.map(s -> s.getMastery().doubleValue()).orElse(0.0);
            if (currentMastery < gate) {
                Concept prereqConcept = conceptDao.findById(pId)
                        .orElseThrow(() -> new IllegalStateException("Prerequisite concept not found: " + pId));
                throw new PrerequisiteNotMetException(prereqConcept.getTitle(), currentMastery, gate);
            }
        }
    }

    public Map<Integer, ConceptStatusView> getAllConceptStatuses(int studentId, int courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        double gate = course.getGateThreshold();

        List<Concept> concepts = conceptDao.findByCourseId(courseId);
        Map<Integer, Set<Integer>> prereqMap = conceptDao.findPrerequisitesMap(courseId);
        Map<Integer, MasterySnapshot> latestSnapshots = masterySnapshotDao.findLatestForCourse(studentId, courseId);
        Map<Integer, List<Evidence>> evidenceMap = evidenceDao.findByStudentAndCourse(studentId, courseId);

        Instant now = Instant.now();
        Map<Integer, ConceptStatusView> viewMap = new LinkedHashMap<>();

        for (Concept c : concepts) {
            int cId = c.getConceptId();
            MasterySnapshot snap = latestSnapshots.get(cId);
            List<Evidence> evList = evidenceMap.getOrDefault(cId, Collections.emptyList());

            Double mastery = null;
            int evidenceCount = 0;
            MasteryLevel level = MasteryLevel.UNSEEN;

            if (snap != null && snap.getEvidenceCount() > 0) {
                mastery = snap.getMastery().doubleValue();
                evidenceCount = snap.getEvidenceCount();
                level = masteryPolicy.levelOf(mastery, evidenceCount);
            }

            boolean needsRefresh = masteryPolicy.needsRefresh(evList, level, now);

            // Check gating against all prerequisites
            boolean locked = false;
            String blockingTitle = null;

            Set<Integer> pSet = prereqMap.getOrDefault(cId, Collections.emptySet());
            for (int pId : pSet) {
                MasterySnapshot pSnap = latestSnapshots.get(pId);
                double pMastery = (pSnap != null) ? pSnap.getMastery().doubleValue() : 0.0;
                if (pMastery < gate) {
                    locked = true;
                    Concept pConcept = concepts.stream().filter(x -> x.getConceptId() == pId).findFirst().orElse(null);
                    blockingTitle = pConcept != null ? pConcept.getTitle() : ("Concept #" + pId);
                    break;
                }
            }

            viewMap.put(cId, new ConceptStatusView(c, mastery, evidenceCount, level, locked, blockingTitle, needsRefresh));
        }

        return viewMap;
    }

    public ConceptStatusView getConceptStatus(int studentId, int courseId, int conceptId) {
        return getAllConceptStatuses(studentId, courseId).get(conceptId);
    }
}
