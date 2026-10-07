package com.sopan.model;

import com.sopan.model.enums.CourseStatus;
import com.sopan.model.enums.Difficulty;

import java.time.LocalDateTime;

public class Course {

    private int courseId;
    private int instructorId;
    private String code;
    private String title;
    private String description;
    private String category;
    private Difficulty difficulty;
    private int maxStudents;
    private int gateThreshold;
    private CourseStatus status;
    private LocalDateTime createdAt;

    // Transient helper filled by queries that join instructor name
    private String instructorName;

    public int getCourseId()                     { return courseId; }
    public void setCourseId(int id)              { this.courseId = id; }

    public int getInstructorId()                 { return instructorId; }
    public void setInstructorId(int id)           { this.instructorId = id; }

    public String getCode()                      { return code; }
    public void setCode(String c)                { this.code = c; }

    public String getTitle()                     { return title; }
    public void setTitle(String t)               { this.title = t; }

    public String getDescription()               { return description; }
    public void setDescription(String d)          { this.description = d; }

    public String getCategory()                  { return category; }
    public void setCategory(String c)            { this.category = c; }

    public Difficulty getDifficulty()            { return difficulty; }
    public void setDifficulty(Difficulty d)       { this.difficulty = d; }

    public int getMaxStudents()                  { return maxStudents; }
    public void setMaxStudents(int m)            { this.maxStudents = m; }

    public int getGateThreshold()                { return gateThreshold; }
    public void setGateThreshold(int g)          { this.gateThreshold = g; }

    public CourseStatus getStatus()              { return status; }
    public void setStatus(CourseStatus s)         { this.status = s; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)     { this.createdAt = t; }

    public String getInstructorName()            { return instructorName; }
    public void setInstructorName(String n)       { this.instructorName = n; }
}
