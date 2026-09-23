package com.nexdesk.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    private DatabaseManager() {
        // Prevent object creation
    }

    /**
     * Gets a required environment variable.
     */
    private static String getEnvironmentVariable(String name)
            throws SQLException {

        String value = System.getenv(name);

        if (value == null || value.trim().isEmpty()) {
            throw new SQLException(
                    name + " environment variable is not configured."
            );
        }

        return value.trim();
    }

    /**
     * Creates a connection to the Oracle database.
     *
     * Render environment variables:
     *
     * DB_URL
     * DB_USER
     * DB_PASSWORD
     */
    public static Connection getConnection() throws SQLException {

        String url = getEnvironmentVariable("DB_URL");
        String user = getEnvironmentVariable("DB_USER");
        String password = getEnvironmentVariable("DB_PASSWORD");

        try {
            return DriverManager.getConnection(
                    url,
                    user,
                    password
            );

        } catch (SQLException e) {

            System.err.println("========================================");
            System.err.println("ORACLE DATABASE CONNECTION FAILED");
            System.err.println("========================================");
            System.err.println("URL  : " + url);
            System.err.println("USER : " + user);
            System.err.println("ERROR: " + e.getMessage());
            System.err.println("========================================");

            throw e;
        }
    }

    /**
     * Tests the Oracle database connection.
     */
    public static void testConnection() throws SQLException {

        try (Connection connection = getConnection()) {

            System.out.println("========================================");
            System.out.println("ORACLE DATABASE CONNECTION SUCCESSFUL");
            System.out.println("========================================");
            System.out.println("Database URL  : " + getEnvironmentVariable("DB_URL"));
            System.out.println("Database User : " + getEnvironmentVariable("DB_USER"));
            System.out.println("========================================");
        }
    }

    /**
     * Returns the configured database URL.
     */
    public static String getUrl() {

        String url = System.getenv("DB_URL");

        if (url == null) {
            return "";
        }

        return url;
    }

    /**
     * Returns the configured database username.
     */
    public static String getUser() {

        String user = System.getenv("DB_USER");

        if (user == null) {
            return "";
        }

        return user;
    }
}
