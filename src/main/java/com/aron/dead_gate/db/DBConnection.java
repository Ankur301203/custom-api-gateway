package com.aron.dead_gate.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/dead_gate";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "ankur";
    static{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading mysql driver", e);
        }
    }

    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
