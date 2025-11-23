package com.exam.dao;

import com.exam.database.DatabaseConnection;
import com.exam.model.Tag;
import com.exam.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    private int getOrCreateTagId(Connection conn, String tagName) throws Exception {
        String findSql = "select id from tags where name=?";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, tagName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }

        String insertSql = "insert into tags(name) values(?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tagName);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        throw new RuntimeException("Cannot create tag");
    }

    private List<Tag> getTagsByTaskId(Connection conn, int taskId) throws Exception {
        List<Tag> tags = new ArrayList<>();
        String sql = """ 
        select tg.id, tg.name from tags tg join task_tags tt ON tg.id = tt.tag_id where tt.task_id = ?;
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        }
        return tags;
    }

    private void clearTagsOfTask(Connection conn, int taskId) throws Exception {
        String sql = "delete from task_tags where task_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    private void insertLinks(Connection conn, int taskId, List<String> tagNames) throws Exception {
        String linkSql = "insert into task_tags(task_id, tag_id) values(?, ?)";

        for (String tagName : tagNames) {
            if (tagName == null) continue;
            String clean = tagName.trim();
            if (clean.isEmpty()) continue;

            int tagId = getOrCreateTagId(conn, clean);

            try (PreparedStatement ps = conn.prepareStatement(linkSql)) {
                ps.setInt(1, taskId);
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }
        }
    }

    public void insertWithTags(Task t, List<String> tagNames) {
        String sqlTask = "insert into tasks(title, description, priority) values(?, ?, ?) ";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int taskId;
            try (PreparedStatement ps = conn.prepareStatement(sqlTask, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getTitle());
                ps.setString(2, t.getDescription());
                ps.setInt(3, t.getPriority());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                taskId = keys.getInt(1);
            }

            insertLinks(conn, taskId, tagNames);

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWithTags(Task t, List<String> tagNames) {
        String sql = "update tasks set title=?, description=?, priority=? where id=?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, t.getTitle());
                ps.setString(2, t.getDescription());
                ps.setInt(3, t.getPriority());
                ps.setInt(4, t.getId());
                ps.executeUpdate();
            }

            clearTagsOfTask(conn, t.getId());
            insertLinks(conn, t.getId(), tagNames);

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Task> getAllWithTags() {
        List<Task> list = new ArrayList<>();
        String sql = "select * from tasks";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Task t = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("priority"));
                t.setTags(getTagsByTaskId(conn, t.getId()));
                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Task> searchWithTags(String keyword) {
        List<Task> list = new ArrayList<>();
        String sql = "select * from tasks where title like ? or description like ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Task t = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("priority"));
                t.setTags(getTagsByTaskId(conn, t.getId()));
                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Task> getAllSortedByPriorityWithTags(boolean asc) {
        List<Task> list = new ArrayList<>();
        String sql = "select * from tasks order by priority " + (asc ? "asc" : "desc");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Task t = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("priority"));
                t.setTags(getTagsByTaskId(conn, t.getId()));
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    public List<Task> getAll() {
        return getAllWithTags();
    }
    public void insert(Task t) {
        insertWithTags(t, List.of());
    }
    public void update(Task t) {
        updateWithTags(t, List.of());
    }

    public void delete(int id) {
        String sql = "delete from tasks where id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }


    public List<Task> searchByTagWithTags(String tagKeyword) {
        List<Task> list = new ArrayList<>();

        String sql = """
        select distinct t.* from tasks t join task_tags tt on t.id = tt.task_id join tags tg on tg.id = tt.tag_id where tg.name like ?;
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + tagKeyword.trim() + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Task t = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getInt("priority"));
                t.setTags(getTagsByTaskId(conn, t.getId()));
                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
