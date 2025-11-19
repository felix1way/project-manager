package com.exam.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/projectFinal";
    private static final String USER = "root";
    private static final String PASSWORD = "felix1way";

    public static Connection getConnection() {

        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (SQLException e) {
            System.out.println("❌ Kết nối thất bại!");
            throw new RuntimeException(e);
        }
    }
}
