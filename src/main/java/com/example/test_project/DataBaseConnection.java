package com.example.test_project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {

    // Correct JDBC URL for MariaDB/MySQL
    private static final String URL = "jdbc:mysql://localhost:3307/art_gallery"; // Use jdbc:mysql:// for MySQL
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}