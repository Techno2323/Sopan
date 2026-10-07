package com.sopan.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads application-wide configuration from db.properties on the classpath.
 * Immutable after construction — safe to share across threads.
 */
public final class AppConfig {

    private static final String PROPS_FILE = "db.properties";

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String dbDriver;

    private AppConfig(Properties props) {
        this.dbUrl      = requireProp(props, "db.url");
        this.dbUser     = requireProp(props, "db.user");
        this.dbPassword = requireProp(props, "db.password");
        this.dbDriver   = requireProp(props, "db.driver");
    }

    /** Loads db.properties from the classpath. */
    public static AppConfig load() {
        Properties props = new Properties();
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(PROPS_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        PROPS_FILE + " not found on classpath. "
                        + "Copy db.properties.example to db.properties and set real credentials.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + PROPS_FILE, e);
        }
        return new AppConfig(props);
    }

    public String getDbUrl()      { return dbUrl; }
    public String getDbUser()     { return dbUser; }
    public String getDbPassword() { return dbPassword; }
    public String getDbDriver()   { return dbDriver; }

    private static String requireProp(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value.trim();
    }
}
