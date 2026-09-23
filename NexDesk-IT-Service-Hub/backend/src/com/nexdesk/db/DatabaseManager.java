package com.nexdesk.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private DatabaseManager() {
        // Prevent object creation
    }

    public static Connection getConnection() throws SQLException {

        if (URL == null || URL.isBlank()) {
            throw new SQLException(
                    "DB_URL environment variable is not configured."
            );
        }

        if (USER == null || USER.isBlank()) {
            throw new SQLException(
                    "DB_USER environment variable is not configured."
            );
        }

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new SQLException(
                    "DB_PASSWORD environment variable is not configured."
            );
        }

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }

    public static void testConnection() throws SQLException {

        try (Connection connection = getConnection()) {

            System.out.println("========================================");
            System.out.println("Oracle connection successful.");
            System.out.println("URL  : " + URL);
            System.out.println("USER : " + USER);
            System.out.println("========================================");
        }
    }
}
