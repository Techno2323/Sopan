package com.sopan.model;

import java.time.LocalDateTime;

public class Assignment {

    private int assignmentId;
    private int courseId;
    private String title;
    private String instructions;
    private LocalDateTime dueAt;
    private boolean allowLate;

    public int getAssignmentId()                 { return assignmentId; }
    public void setAssignmentId(int id)          { this.assignmentId = id; }

    public int getCourseId()                     { return courseId; }
    public void setCourseId(int id)              { this.courseId = id; }

    public String getTitle()                     { return title; }
    public void setTitle(String t)               { this.title = t; }

    public String getInstructions()              { return instructions; }
    public void setInstructions(String i)         { this.instructions = i; }

    public LocalDateTime getDueAt()              { return dueAt; }
    public void setDueAt(LocalDateTime d)         { this.dueAt = d; }

    public boolean isAllowLate()                 { return allowLate; }
    public void setAllowLate(boolean a)          { this.allowLate = a; }

    public boolean isOverdue() {
        return dueAt != null && LocalDateTime.now().isAfter(dueAt);
    }
}
