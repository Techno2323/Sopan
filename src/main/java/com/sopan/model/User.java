package com.sopan.model;

import com.sopan.model.enums.AccountStatus;
import com.sopan.model.enums.Role;

import java.time.LocalDateTime;

/**
 * Base of the user hierarchy. Each concrete subclass knows its home path
 * and which URL prefixes it may access.
 */
public abstract class User {

    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private Role role;
    private AccountStatus status;
    private LocalDateTime createdAt;

    protected User() { }

    protected User(int id, String fullName, String email, Role role) {
        this.id       = id;
        this.fullName = fullName;
        this.email    = email;
        this.role     = role;
        this.status   = AccountStatus.ACTIVE;
    }

    /** Dashboard URL for this role. */
    public abstract String homePath();

    /** True if this role is allowed to reach the given request path. */
    public abstract boolean canAccess(String path);

    // ── getters and setters ───────────────────────────────────

    public int getId()                     { return id; }
    public void setId(int id)              { this.id = id; }

    public String getFullName()            { return fullName; }
    public void setFullName(String n)      { this.fullName = n; }

    public String getEmail()               { return email; }
    public void setEmail(String e)         { this.email = e; }

    public String getPasswordHash()        { return passwordHash; }
    public void setPasswordHash(String h)  { this.passwordHash = h; }

    public String getPasswordSalt()        { return passwordSalt; }
    public void setPasswordSalt(String s)  { this.passwordSalt = s; }

    public Role getRole()                  { return role; }
    public void setRole(Role r)            { this.role = r; }

    public AccountStatus getStatus()       { return status; }
    public void setStatus(AccountStatus s) { this.status = s; }

    public LocalDateTime getCreatedAt()    { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}
