package com.sopan.model;

import com.sopan.model.enums.Role;

/**
 * Student with profile fields. Accesses /learn/** and /catalog/**.
 */
public class Student extends User {

    private String rollNo;
    private String program;
    private int studyYear;

    public Student() { setRole(Role.STUDENT); }

    public Student(int id, String fullName, String email) {
        super(id, fullName, email, Role.STUDENT);
    }

    @Override
    public String homePath() { return "/learn/home"; }

    @Override
    public boolean canAccess(String path) {
        return path.startsWith("/learn")
            || path.startsWith("/catalog")
            || path.equals("/logout");
    }

    public String getRollNo()          { return rollNo; }
    public void setRollNo(String r)    { this.rollNo = r; }

    public String getProgram()         { return program; }
    public void setProgram(String p)   { this.program = p; }

    public int getStudyYear()          { return studyYear; }
    public void setStudyYear(int y)    { this.studyYear = y; }
}
