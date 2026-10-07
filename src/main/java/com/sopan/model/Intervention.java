package com.sopan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Intervention {

    public enum Type {
        NOTE, EXTRA_MATERIAL, OFFICE_HOURS, RETAKE
    }

    private int interventionId;
    private int courseId;
    private int conceptId;
    private int studentId;
    private int instructorId;
    private Type type;
    private String note;
    private BigDecimal masteryBefore;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private BigDecimal masteryAfter;

    // Transient helpers
    private String studentName;
    private String conceptTitle;

    public int getInterventionId() { return interventionId; }
    public void setInterventionId(int interventionId) { this.interventionId = interventionId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getConceptId() { return conceptId; }
    public void setConceptId(int conceptId) { this.conceptId = conceptId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getInstructorId() { return instructorId; }
    public void setInstructorId(int instructorId) { this.instructorId = instructorId; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public BigDecimal getMasteryBefore() { return masteryBefore; }
    public void setMasteryBefore(BigDecimal masteryBefore) { this.masteryBefore = masteryBefore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public BigDecimal getMasteryAfter() { return masteryAfter; }
    public void setMasteryAfter(BigDecimal masteryAfter) { this.masteryAfter = masteryAfter; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getConceptTitle() { return conceptTitle; }
    public void setConceptTitle(String conceptTitle) { this.conceptTitle = conceptTitle; }
}
