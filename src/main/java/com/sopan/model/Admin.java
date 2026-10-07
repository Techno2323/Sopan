package com.sopan.model;

import com.sopan.model.enums.Role;

/**
 * Admin user. Accesses /admin/**, /catalog/**, and all management paths.
 */
public class Admin extends User {

    public Admin() { setRole(Role.ADMIN); }

    public Admin(int id, String fullName, String email) {
        super(id, fullName, email, Role.ADMIN);
    }

    @Override
    public String homePath() { return "/admin/home"; }

    @Override
    public boolean canAccess(String path) {
        return path.startsWith("/admin")
            || path.startsWith("/catalog")
            || path.equals("/logout");
    }
}
