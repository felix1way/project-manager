package com.exam.ui;

import com.exam.dao.TaskDAO;
import com.exam.model.Task;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class TaskSwingApp extends JFrame {

    private TaskDAO dao = new TaskDAO();
    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSearch;
    private JComboBox<String> cbTags;

    private void loadData() {
        model.setRowCount(0);
        List<Task> list = dao.getAllTasks();
        for (Task t : list) {
            model.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getPriority(),
                    t.tagAsString(),
                    t.getDeadline()
            });
        }
    }

    private void searchTasks() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        model.setRowCount(0);
        List<Task> list = dao.searchWithTags(keyword);
        for (Task t : list) {
            model.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getPriority(),
                    t.tagAsString(),
                    t.getDeadline()
            });
        }
    }

    private void searchByTagCombo(String tag) {
        if (tag == null || tag.equals("All")) {
            loadData();
            return;
        }

        model.setRowCount(0);
        List<Task> list = dao.searchByTagWithTags(tag);
        for (Task t : list) {
            model.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getPriority(),
                    t.tagAsString(),
                    t.getDeadline()
            });
        }
    }

    private void loadTagsToComboBox(JComboBox<String> cb) {
        cb.removeAllItems();
        cb.addItem("All");
        List<String> tags = dao.getAllTagNames();
        for (String t : tags) cb.addItem(t);
    }

    private void sortTasks(boolean asc) {
        model.setRowCount(0);
        List<Task> list = dao.getAllSortedByPriorityWithTags(asc);
        for (Task t : list) {
            model.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getPriority(),
                    t.tagAsString(),
                    t.getDeadline()
            });
        }
    }

    private void addTask() {

        String title = JOptionPane.showInputDialog("Title:");
        if (title == null || title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required!");
            return;
        }

        String desc = JOptionPane.showInputDialog("Description:");
        if (desc == null || desc.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description is required!");
            return;
        }

        String prStr = JOptionPane.showInputDialog("Priority (1-5):");
        if (prStr == null || prStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Priority is required!");
            return;
        }

        int pr;
        try {
            pr = Integer.parseInt(prStr);
            if (pr < 1 || pr > 5) {
                JOptionPane.showMessageDialog(this, "Priority must be between 1 and 5!");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Priority must be a number!");
            return;
        }

        String tagInput = JOptionPane.showInputDialog("Tags (vd: oop,jdbc,...):");
        if (tagInput == null || tagInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "At least ONE tag is required!");
            return;
        }

        List<String> tagNames = Arrays.stream(tagInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (tagNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid tag format!");
            return;
        }

        String deadlineStr = JOptionPane.showInputDialog("Deadline (yyyy-MM-dd):");
        if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Deadline is required!");
            return;
        }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(deadlineStr);
            if (deadline.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Deadline cannot be in the past!");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Deadline must be yyyy-MM-dd");
            return;
        }

        dao.insertWithTags(new Task(title, desc, pr, deadline), tagNames);

        loadData();
        loadTagsToComboBox(cbTags);
    }

    private void updateTask() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to update");
            return;
        }

        int id = (int) table.getValueAt(row, 0);

        String newTitle = JOptionPane.showInputDialog("New Title:", table.getValueAt(row, 1));
        if (newTitle == null || newTitle.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty!");
            return;
        }

        String newDesc = JOptionPane.showInputDialog("New Description:", table.getValueAt(row, 2));
        if (newDesc == null || newDesc.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Description cannot be empty!");
            return;
        }

        String prStr = JOptionPane.showInputDialog("New Priority (1-5):", table.getValueAt(row, 3));
        int newPriority;
        try {
            newPriority = Integer.parseInt(prStr);
            if (newPriority < 1 || newPriority > 5) {
                JOptionPane.showMessageDialog(this, "Priority must be between 1 and 5!");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Priority must be a number!");
            return;
        }

        String newTags = JOptionPane.showInputDialog(
                "New Tags (vd: oop,jdbc,...):", table.getValueAt(row, 4)
        );

        if (newTags == null || newTags.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "At least ONE tag is required!");
            return;
        }

        List<String> tagNames = Arrays.stream(newTags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (tagNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid tag format!");
            return;
        }

        String deadlineStr = JOptionPane.showInputDialog(
                "New Deadline (yyyy-MM-dd):", table.getValueAt(row, 5)
        );

        LocalDate newDeadline;
        try {
            newDeadline = LocalDate.parse(deadlineStr);
            if (newDeadline.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Deadline cannot be in the past!");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Deadline must be yyyy-MM-dd");
            return;
        }

        dao.updateWithTags(new Task(id, newTitle, newDesc, newPriority, newDeadline), tagNames);

        loadData();
        loadTagsToComboBox(cbTags);
    }

    private void deleteTask() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) table.getValueAt(row, 0);
        dao.delete(id);

        loadData();
        loadTagsToComboBox(cbTags);
    }

    public TaskSwingApp() {
        setTitle("Task Manager");
        setSize(900, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Description", "Priority", "Tags", "Deadline"}, 0
        );

        table = new JTable(model);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                    c.setBackground(table.getSelectionBackground());
                    return c;
                }

                c.setForeground(Color.BLACK);
                c.setBackground(Color.WHITE);

                Object deadlineObj = table.getModel().getValueAt(row, 5);
                if (deadlineObj instanceof LocalDate deadline) {
                    if (deadline.isBefore(LocalDate.now())) {
                        c.setForeground(Color.RED);
                    }
                }

                return c;
            }
        });


        loadData();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        JButton btnClear = new JButton("Clear");

        btnSearch.addActionListener(e -> searchTasks());
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            cbTags.setSelectedItem("All");
            loadData();
        });

        topPanel.add(new JLabel("Keyword:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnClear);

        cbTags = new JComboBox<>();
        loadTagsToComboBox(cbTags);
        cbTags.addActionListener(e -> searchByTagCombo((String) cbTags.getSelectedItem()));

        topPanel.add(new JLabel("Tag:"));
        topPanel.add(cbTags);

        JComboBox<String> cbSort = new JComboBox<>(new String[]{"Priority Asc", "Priority Desc"});
        cbSort.addActionListener(e -> sortTasks(cbSort.getSelectedIndex() == 0));

        topPanel.add(new JLabel("Sort:"));
        topPanel.add(cbSort);

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");

        btnAdd.addActionListener(e -> addTask());
        btnUpdate.addActionListener(e -> updateTask());
        btnDelete.addActionListener(e -> deleteTask());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnUpdate);
        bottomPanel.add(btnDelete);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        new TaskSwingApp().setVisible(true);
    }
}
