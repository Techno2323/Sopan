package com.sopan.model;

public class Quiz {

    private int quizId;
    private int courseId;
    private String title;
    private int maxAttempts;
    private String status; // DRAFT or PUBLISHED

    public int getQuizId()               { return quizId; }
    public void setQuizId(int id)        { this.quizId = id; }

    public int getCourseId()             { return courseId; }
    public void setCourseId(int id)      { this.courseId = id; }

    public String getTitle()             { return title; }
    public void setTitle(String t)       { this.title = t; }

    public int getMaxAttempts()          { return maxAttempts; }
    public void setMaxAttempts(int m)    { this.maxAttempts = m; }

    public String getStatus()            { return status; }
    public void setStatus(String s)      { this.status = s; }
}
