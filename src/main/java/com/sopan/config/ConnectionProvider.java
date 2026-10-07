package com.sopan.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstraction over JDBC connection acquisition.
 * Production uses DriverManagerProvider; tests can substitute a pool or mock.
 */
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
