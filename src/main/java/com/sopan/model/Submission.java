package com.sopan.model;

import java.time.LocalDateTime;

public class Submission {

    private int submissionId;
    private int assignmentId;
    private int studentId;
    private String bodyText;
    private String filePath;
    private LocalDateTime submittedAt;
    private boolean late;
    private String status; // SUBMITTED or GRADED
    private LocalDateTime gradedAt;
    private String feedback;

    // Transient helpers
    private String studentName;
    private String assignmentTitle;

    public int getSubmissionId()                  { return submissionId; }
    public void setSubmissionId(int id)           { this.submissionId = id; }

    public int getAssignmentId()                  { return assignmentId; }
    public void setAssignmentId(int id)           { this.assignmentId = id; }

    public int getStudentId()                     { return studentId; }
    public void setStudentId(int id)              { this.studentId = id; }

    public String getBodyText()                   { return bodyText; }
    public void setBodyText(String b)             { this.bodyText = b; }

    public String getFilePath()                   { return filePath; }
    public void setFilePath(String f)             { this.filePath = f; }

    public LocalDateTime getSubmittedAt()         { return submittedAt; }
    public void setSubmittedAt(LocalDateTime t)    { this.submittedAt = t; }

    public boolean isLate()                       { return late; }
    public void setLate(boolean l)                { this.late = l; }

    public String getStatus()                     { return status; }
    public void setStatus(String s)               { this.status = s; }

    public LocalDateTime getGradedAt()            { return gradedAt; }
    public void setGradedAt(LocalDateTime t)       { this.gradedAt = t; }

    public String getFeedback()                   { return feedback; }
    public void setFeedback(String f)             { this.feedback = f; }

    public String getStudentName()                { return studentName; }
    public void setStudentName(String n)          { this.studentName = n; }

    public String getAssignmentTitle()            { return assignmentTitle; }
    public void setAssignmentTitle(String t)      { this.assignmentTitle = t; }
}
