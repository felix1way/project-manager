package com.exam.dao;

import com.exam.database.DatabaseConnection;
import com.exam.model.Task;
import com.exam.model.Tag;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO implements ITaskDAO {

    // =========================
    // GET OR CREATE TAG
    // =========================
    private Integer getOrCreateTagId(Connection conn, String tagName) throws SQLException {
        // 1. Check existing tag
        String selectSql = "SELECT id FROM tags WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, tagName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }

        // 2. Insert new tag
        String insertSql = "INSERT INTO tags(name) VALUES(?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tagName);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return null;
    }

    // =========================
    // GET ALL TASKS
    // =========================
    @Override
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();

        String sql = """
                SELECT t.id, t.title, t.description, t.priority, t.deadline,
                       g.id AS tag_id, g.name AS tag_name
                FROM tasks t
                LEFT JOIN tags g ON t.tag_id = g.id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tasks;
    }

    // =========================
    // INSERT TASK (FIXED)
    // =========================
    @Override
    public void insert(Task task) {
        String sql = """
                INSERT INTO tasks (title, description, priority, deadline, tag_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getPriority());

            if (task.getDeadline() != null) {
                ps.setDate(4, Date.valueOf(task.getDeadline()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            if (task.getTag() != null) {
                Integer tagId = getOrCreateTagId(conn, task.getTag().getName());
                ps.setInt(5, tagId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // UPDATE TASK (FIXED)
    // =========================
    @Override
    public void update(Task task) {
        String sql = """
                UPDATE tasks
                SET title = ?, description = ?, priority = ?, deadline = ?, tag_id = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getPriority());

            if (task.getDeadline() != null) {
                ps.setDate(4, Date.valueOf(task.getDeadline()));
            } else {
                ps.setNull(4, Types.DATE);
            }

            if (task.getTag() != null) {
                Integer tagId = getOrCreateTagId(conn, task.getTag().getName());
                ps.setInt(5, tagId);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(6, task.getId());
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // DELETE TASK
    // =========================
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Task> search(String keyword) {
        List<Task> tasks = new ArrayList<>();

        String sql = """
            SELECT t.id, t.title, t.description, t.priority, t.deadline,
                   g.id AS tag_id, g.name AS tag_name
            FROM tasks t
            LEFT JOIN tags g ON t.tag_id = g.id
            WHERE t.title LIKE ? OR t.description LIKE ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String key = "%" + keyword + "%";
            ps.setString(1, key);
            ps.setString(2, key);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tasks;
    }


    // =========================
    // SORT BY PRIORITY
    // =========================
    public List<Task> sortByPriority(boolean asc) {
        List<Task> tasks = new ArrayList<>();
        String order = asc ? "ASC" : "DESC";

        String sql = """
                SELECT t.id, t.title, t.description, t.priority, t.deadline,
                       g.id AS tag_id, g.name AS tag_name
                FROM tasks t
                LEFT JOIN tags g ON t.tag_id = g.id
                ORDER BY t.priority %s
                """.formatted(order);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tasks;
    }

    // =========================
    // MAP RESULTSET
    // =========================
    private Task mapRowToTask(ResultSet rs) throws Exception {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPriority(rs.getInt("priority"));

        Date date = rs.getDate("deadline");
        if (date != null) {
            task.setDeadline(date.toLocalDate());
        }

        int tagId = rs.getInt("tag_id");
        if (!rs.wasNull()) {
            task.setTag(new Tag(tagId, rs.getString("tag_name")));
        }

        return task;
    }
}
