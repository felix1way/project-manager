package com.exam.ui;

import com.exam.dao.TaskDAO;
import com.exam.model.Task;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TaskSwingApp extends JFrame {

    private TaskDAO dao = new TaskDAO();
    private JTable table;
    private DefaultTableModel model;

    public TaskSwingApp() {
        setTitle("Task Manager");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        model = new DefaultTableModel(new Object[]{"ID", "Title", "Description", "Priority"}, 0);
        table = new JTable(model);
        loadData();

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> addTask());

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(e -> updateTask());

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteTask());

        JPanel panel = new JPanel();
        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
    }

    private void loadData() {
        model.setRowCount(0);
        List<Task> list = dao.getAll();
        for (Task t : list) {
            model.addRow(new Object[]{t.getId(), t.getTitle(), t.getDescription(), t.getPriority()});
        }
    }

    private void addTask() {
        String title = JOptionPane.showInputDialog("Title:");
        String desc = JOptionPane.showInputDialog("Description:");
        int pr = Integer.parseInt(JOptionPane.showInputDialog("Priority:"));

        dao.insert(new Task(title, desc, pr));
        loadData();
    }

    private void updateTask() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to update");
            return;
        }
        int id = (int) table.getValueAt(row, 0);

        String newTitle = JOptionPane.showInputDialog("New Title:", table.getValueAt(row, 1));
        String newDesc = JOptionPane.showInputDialog("New Description:", table.getValueAt(row, 2));
        int newPriority = Integer.parseInt(JOptionPane.showInputDialog("New Priority:", table.getValueAt(row, 3)));

        dao.update(new Task(id, newTitle, newDesc, newPriority));
        loadData();
    }


    private void deleteTask() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) table.getValueAt(row, 0);
        dao.delete(id);
        loadData();
    }

    public static void main(String[] args) {
        new TaskSwingApp().setVisible(true);
    }
}
