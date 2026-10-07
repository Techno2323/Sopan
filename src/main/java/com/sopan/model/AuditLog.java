package com.sopan.model;

import java.time.LocalDateTime;

public class AuditLog {

    private long logId;
    private Integer userId;
    private String action;
    private String entity;
    private Integer entityId;
    private LocalDateTime createdAt;

    // Transient helper
    private String userEmail;

    public long getLogId() { return logId; }
    public void setLogId(long logId) { this.logId = logId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
