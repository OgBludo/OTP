package com.otp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String URL = "yourURL";
    private static final String USER = "youruser";
    private static final String PASSWORD = "yourpassword";
    private static final String SCHEMA = "service";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        conn.createStatement().execute("SET search_path TO " + SCHEMA);
        return conn;
    }
}
