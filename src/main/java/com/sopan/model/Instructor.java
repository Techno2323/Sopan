package com.sopan.model;

import com.sopan.model.enums.Role;

/**
 * Instructor with department and bio. Accesses /teach/** and /catalog/**.
 */
public class Instructor extends User {

    private String department;
    private String bio;

    public Instructor() { setRole(Role.INSTRUCTOR); }

    public Instructor(int id, String fullName, String email) {
        super(id, fullName, email, Role.INSTRUCTOR);
    }

    @Override
    public String homePath() { return "/teach/home"; }

    @Override
    public boolean canAccess(String path) {
        return path.startsWith("/teach")
            || path.startsWith("/catalog")
            || path.equals("/logout");
    }

    public String getDepartment()        { return department; }
    public void setDepartment(String d)  { this.department = d; }

    public String getBio()               { return bio; }
    public void setBio(String b)         { this.bio = b; }
}
