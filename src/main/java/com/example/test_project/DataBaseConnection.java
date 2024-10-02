//package com.example.test_project;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class DataBaseConnection {
//
//    // Correct JDBC URL for MariaDB/MySQL
//    private static final String URL = "jdbc:mysql://localhost:3306/art_gallery"; // Use jdbc:mysql:// for MySQL
//    private static final String USER = "root"; // default username for XAMPP
//    private static final String PASSWORD = ""; // default password is empty
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(URL, USER, PASSWORD);
//    }
//}

package com.example.test_project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {

    // Correct JDBC URL for MariaDB/MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/art_gallery"; // Use jdbc:mysql:// for MySQL
    private static final String USER = "root"; // default username for XAMPP
    private static final String PASSWORD = ""; // default password is empty

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}

