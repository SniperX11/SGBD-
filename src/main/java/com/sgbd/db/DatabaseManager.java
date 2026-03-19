package com.sgbd.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton-style factory that provides JDBC connections to PostgreSQL.
 *
 * Connection parameters are read from the file
 * {@code src/main/resources/db.properties} on the classpath.
 *
 * Each call to {@link #getConnection()} opens a fresh connection.
 * Callers are responsible for closing it (preferably with try-with-resources).
 */
public class DatabaseManager {

    /** Path to the properties file inside the classpath. */
    private static final String PROPERTIES_FILE = "/db.properties";

    private static String url;
    private static String username;
    private static String password;

    // Load connection parameters once at class-initialisation time
    static {
        try (InputStream is = DatabaseManager.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) {
                throw new ExceptionInInitializerError(
                        "Cannot find " + PROPERTIES_FILE + " on classpath. " +
                        "Copy src/main/resources/db.properties.example to " +
                        "src/main/resources/db.properties and fill in your credentials.");
            }
            Properties props = new Properties();
            props.load(is);
            url      = props.getProperty("db.url");
            username = props.getProperty("db.username");
            password = props.getProperty("db.password");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** Utility class – no instances needed. */
    private DatabaseManager() {}

    /**
     * Opens and returns a new JDBC {@link Connection}.
     * The caller MUST close this connection after use.
     *
     * @return a fresh {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
