package com.exam.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/projectFinal";
        String user = "root";
        String password = "felix1way";

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Kết nối thành công!");
            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Kết nối thất bại!");
            e.printStackTrace();
        }
    }
}
