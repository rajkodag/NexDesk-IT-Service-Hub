package com.nexdesk.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    // Database configuration is read from environment variables.
    // These variables will be configured in Render.
    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    // Prevent object creation
    private DatabaseManager() {
    }

    /**
     * Creates a connection to the Oracle database.
     */
    public static Connection getConnection() throws SQLException {

        // Check DB_URL
        if (URL == null || URL.isBlank()) {
            throw new SQLException(
                    "DB_URL environment variable is not configured."
            );
        }

        // Check DB_USER
        if (USER == null || USER.isBlank()) {
            throw new SQLException(
                    "DB_USER environment variable is not configured."
            );
        }

        // Check DB_PASSWORD
        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "DB_PASSWORD environment variable is not configured."
            );
        }

        try {

            Connection connection = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            return connection;

        } catch (SQLException e) {

            System.err.println("========================================");
            System.err.println("DATABASE CONNECTION FAILED");
            System.err.println("========================================");
            System.err.println("URL  : " + URL);
            System.err.println("USER : " + USER);
            System.err.println("ERROR: " + e.getMessage());
            System.err.println("========================================");

            throw e;
        }
    }

    /**
     * Returns the configured database URL.
     */
    public static String getUrl() {
        return URL;
    }

    /**
     * Returns the configured database username.
     */
    public static String getUser() {
        return USER;
    }

    /**
     * Tests the database connection.
     */
    public static void testConnection() throws SQLException {

        try (Connection connection = getConnection()) {

            if (connection != null && !connection.isClosed()) {

                System.out.println("========================================");
                System.out.println("Oracle connection successful.");
                System.out.println("========================================");
                System.out.println("URL  : " + URL);
                System.out.println("USER : " + USER);
                System.out.println("========================================");
            }
        }
    }
}
