package com.sopan.service;

import com.sopan.dao.*;
import com.sopan.dto.ConceptStatusView;
import com.sopan.dto.DashboardView;
import com.sopan.dto.RootCauseView;
import com.sopan.engine.*;
import com.sopan.model.Assignment;
import com.sopan.model.Course;
import com.sopan.model.Enrollment;
import com.sopan.model.Quiz;
import com.sopan.model.enums.MasteryLevel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class RecommendationService {

    private final EnrollmentDao enrollmentDao;
    private final CourseDao courseDao;
    private final ConceptService conceptService;
    private final MasteryService masteryService;
    private final QuizDao quizDao;
    private final AssignmentDao assignmentDao;
    private final NotificationDao notificationDao;
    private final ActionRanker actionRanker = new ActionRanker();
    private final RootCauseAnalyzer rootCauseAnalyzer = new RootCauseAnalyzer();

    public RecommendationService(EnrollmentDao enrollmentDao, CourseDao courseDao,
                                 ConceptService conceptService, MasteryService masteryService,
                                 QuizDao quizDao, AssignmentDao assignmentDao,
                                 NotificationDao notificationDao) {
        this.enrollmentDao = enrollmentDao;
        this.courseDao = courseDao;
        this.conceptService = conceptService;
        this.masteryService = masteryService;
        this.quizDao = quizDao;
        this.assignmentDao = assignmentDao;
        this.notificationDao = notificationDao;
    }

    public List<Action> getTodaysMoves(int studentId, int courseId) {
        ConceptGraph graph = conceptService.getConceptGraph(courseId);
        Map<Integer, ConceptStatusView> statuses = masteryService.getAllConceptStatuses(studentId, courseId);
        List<Quiz> quizzes = quizDao.findPublishedByCourseId(courseId);
        List<Assignment> assignments = assignmentDao.findByCourseId(courseId);

        List<Action> candidates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. Candidate assignment actions
        for (Assignment a : assignments) {
            double urgency = 0.0;
            if (a.getDueAt() != null) {
                long hours = Duration.between(now, a.getDueAt()).toHours();
                if (hours <= 24) urgency = 1.0;
                else if (hours <= 72) urgency = 0.6;
                else if (hours <= 168) urgency = 0.2;
            }
            double priority = ActionRanker.computeScore(urgency, 0.5, 0.0, false);
            candidates.add(new SubmitAssignmentAction(a.getAssignmentId(), a.getTitle(), priority,
                    "Due soon: complete assignment before " + a.getDueAt()));
        }

        // 2. Candidate concept reviews, quizzes, and refreshes
        for (ConceptStatusView sv : statuses.values()) {
            int cId = sv.getConceptId();
            double mastery = sv.getMastery() != null ? sv.getMastery() : 0.0;

            // Gap severity: (60 - mastery)/60 floored at 0
            double gapSeverity = Math.max(0.0, (60.0 - mastery) / 60.0);

            // Unblock value: count descendant concepts currently locked because of this one
            int lockedDescendants = 0;
            for (int descId : graph.descendantsOf(cId)) {
                ConceptStatusView descView = statuses.get(descId);
                if (descView != null && descView.isLocked()) {
                    lockedDescendants++;
                }
            }
            double unblockValue = Math.min(1.0, lockedDescendants / 5.0);

            // Needs refresh action
            if (sv.isNeedsRefresh()) {
                double prio = ActionRanker.computeScore(0.0, 0.1, 0.2, true);
                candidates.add(new RefreshConceptAction(cId, sv.getTitle(), prio,
                        "Maintain your solid foundation in " + sv.getTitle() + " (inactive for 21+ days)"));
            }

            // Shaky concept review
            if (sv.getLevel() == MasteryLevel.SHAKY && !sv.isLocked()) {
                double prio = ActionRanker.computeScore(0.0, gapSeverity, unblockValue, false);
                candidates.add(new ReviewConceptAction(cId, sv.getTitle(), prio,
                        "Strengthen understanding in " + sv.getTitle() + " to unblock advanced topics"));
            }
        }

        // 3. Quizzes for unlocked concepts
        for (Quiz q : quizzes) {
            double prio = ActionRanker.computeScore(0.2, 0.4, 0.4, false);
            candidates.add(new TakeQuizAction(q.getQuizId(), q.getTitle(), prio,
                    "Test your knowledge and build evidence in " + q.getTitle()));
        }

        return actionRanker.rankTopMoves(candidates, 3);
    }

    public List<RootCauseView> getGapRadar(int studentId, int courseId) {
        ConceptGraph graph = conceptService.getConceptGraph(courseId);
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        Map<Integer, Double> masteryMap = masteryService.getMasteryMap(studentId, courseId);
        return rootCauseAnalyzer.analyzeGaps(graph, masteryMap, course.getGateThreshold());
    }

    public DashboardView getDashboard(int studentId) {
        List<Enrollment> enrollments = enrollmentDao.findByStudentId(studentId);
        List<Course> enrolledCourses = new ArrayList<>();
        List<Action> allMoves = new ArrayList<>();
        int totalSolid = 0;
        int totalShaky = 0;

        for (Enrollment e : enrollments) {
            if (e.getStatus() == Enrollment.Status.ACTIVE) {
                courseDao.findById(e.getCourseId()).ifPresent(enrolledCourses::add);
                allMoves.addAll(getTodaysMoves(studentId, e.getCourseId()));
                Map<Integer, ConceptStatusView> statuses = masteryService.getAllConceptStatuses(studentId, e.getCourseId());
                for (ConceptStatusView sv : statuses.values()) {
                    if (sv.getLevel() == MasteryLevel.SOLID) totalSolid++;
                    if (sv.getLevel() == MasteryLevel.SHAKY) totalShaky++;
                }
            }
        }

        List<Action> top3 = actionRanker.rankTopMoves(allMoves, 3);
        List<Assignment> deadlines = assignmentDao.findUpcomingByStudentId(studentId, 5);
        int unreadNotifs = notificationDao.countUnreadByUserId(studentId);

        return new DashboardView(top3, deadlines, enrolledCourses, totalSolid, totalShaky, unreadNotifs);
    }
}
