package com.aron.dead_gate.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/dead_gate";
    private static final String USER = "root";
    private static final String PASSWORD = "ankur";

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("DB Connection established.");
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to DB: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
