package com.sopan.dto;

import com.sopan.engine.Action;
import com.sopan.model.Assignment;
import com.sopan.model.Course;

import java.util.List;

public class DashboardView {

    private final List<Action> topMoves;
    private final List<Assignment> upcomingDeadlines;
    private final List<Course> enrolledCourses;
    private final int totalSolidConcepts;
    private final int totalShakyConcepts;
    private final int unreadNotificationsCount;

    public DashboardView(List<Action> topMoves, List<Assignment> upcomingDeadlines,
                         List<Course> enrolledCourses, int totalSolidConcepts,
                         int totalShakyConcepts, int unreadNotificationsCount) {
        this.topMoves = topMoves;
        this.upcomingDeadlines = upcomingDeadlines;
        this.enrolledCourses = enrolledCourses;
        this.totalSolidConcepts = totalSolidConcepts;
        this.totalShakyConcepts = totalShakyConcepts;
        this.unreadNotificationsCount = unreadNotificationsCount;
    }

    public List<Action> getTopMoves() { return topMoves; }
    public List<Assignment> getUpcomingDeadlines() { return upcomingDeadlines; }
    public List<Course> getEnrolledCourses() { return enrolledCourses; }
    public int getTotalSolidConcepts() { return totalSolidConcepts; }
    public int getTotalShakyConcepts() { return totalShakyConcepts; }
    public int getUnreadNotificationsCount() { return unreadNotificationsCount; }
}
