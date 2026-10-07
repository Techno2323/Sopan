package com.sopan.dao;

import com.sopan.model.AuditLog;

import java.sql.Connection;
import java.util.List;

public interface AuditLogDao {

    long log(Connection conn, Integer userId, String action, String entity, Integer entityId);
    long log(Integer userId, String action, String entity, Integer entityId);

    List<AuditLog> findRecent(int limit);
    List<AuditLog> findRecent(Connection conn, int limit);
}
