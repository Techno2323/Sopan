package com.sopan.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple DriverManager-based provider. Loads the JDBC driver once,
 * then hands out connections from DriverManager on each call.
 */
public final class DriverManagerProvider implements ConnectionProvider {

    private final String url;
    private final String user;
    private final String password;

    public DriverManagerProvider(AppConfig config) {
        this.url      = config.getDbUrl();
        this.user     = config.getDbUser();
        this.password = config.getDbPassword();

        // Load the driver class so DriverManager can find it
        try {
            Class.forName(config.getDbDriver());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("JDBC driver not on classpath: " + config.getDbDriver(), e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
