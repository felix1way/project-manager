package com.exam.dao;

import com.exam.database.DatabaseConnection;
import com.exam.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> getAll() {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("priority")
                ));
            }

        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    public void insert(Task t) {
        String sql = "INSERT INTO tasks(title, description, priority) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setInt(3, t.getPriority());
            ps.executeUpdate();

        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Task t) {
        String sql = "UPDATE tasks SET title=?, description=?, priority=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setInt(3, t.getPriority());
            ps.setInt(4, t.getId());
            ps.executeUpdate();

        } catch (Exception e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM tasks WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) { e.printStackTrace(); }
    }
}
