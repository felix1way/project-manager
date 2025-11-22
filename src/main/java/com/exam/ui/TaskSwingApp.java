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

    private JTextField txtSearch;
    private JTextField txtTagSearch;

    public TaskSwingApp() {
        setTitle("Task Manager");
        setSize(800, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Description", "Priority", "Tags"}, 0
        );
        table = new JTable(model);
        loadData();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        JButton btnClear = new JButton("Clear");

        btnSearch.addActionListener(e -> searchTasks());
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            txtTagSearch.setText("");
            loadData();
        });

        topPanel.add(new JLabel("Keyword:"));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnClear);

        txtTagSearch = new JTextField(10);
        JButton btnSearchTag = new JButton("Search Tag");
        btnSearchTag.addActionListener(e -> searchByTag());

        topPanel.add(new JLabel("Tag:"));
        topPanel.add(txtTagSearch);
        topPanel.add(btnSearchTag);

        String[] sortOptions = {"Priority Asc", "Priority Desc"};
        JComboBox<String> cbSort = new JComboBox<>(sortOptions);
        cbSort.addActionListener(e -> sortTasks(cbSort.getSelectedIndex() == 0));

        topPanel.add(new JLabel("Sort:"));
        topPanel.add(cbSort);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> addTask());

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(e -> updateTask());

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteTask());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnUpdate);
        bottomPanel.add(btnDelete);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }


    private void loadData() {
        model.setRowCount(0);
        List<Task> list = dao.getAllWithTags();
        for (Task t : list) {
            model.addRow(new Object[]{
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getPriority(),
                    t.tagsAsString()
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
                    t.getId(), t.getTitle(), t.getDescription(),
                    t.getPriority(), t.tagsAsString()
            });
        }
    }

    private void searchByTag() {
        String tagKey = txtTagSearch.getText().trim();
        if (tagKey.isEmpty()) {
            loadData();
            return;
        }

        model.setRowCount(0);
        List<Task> list = dao.searchByTagWithTags(tagKey);
        for (Task t : list) {
            model.addRow(new Object[]{
                    t.getId(), t.getTitle(), t.getDescription(),
                    t.getPriority(), t.tagsAsString()
            });
        }
    }

    private void sortTasks(boolean asc) {
        model.setRowCount(0);
        List<Task> list = dao.getAllSortedByPriorityWithTags(asc);
        for (Task t : list) {
            model.addRow(new Object[]{
                    t.getId(), t.getTitle(), t.getDescription(),
                    t.getPriority(), t.tagsAsString()
            });
        }
    }

    private void addTask() {
        String title = JOptionPane.showInputDialog("Title:");
        if (title == null) return;

        String desc = JOptionPane.showInputDialog("Description:");
        if (desc == null) return;

        int pr = Integer.parseInt(JOptionPane.showInputDialog("Priority:"));

        String tagInput = JOptionPane.showInputDialog("Tags (vd: oop,jdbc,urgent):");
        if (tagInput == null) tagInput = "";

        List<String> tagNames = List.of(tagInput.split(","));

        dao.insertWithTags(new Task(title, desc, pr), tagNames);
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
        if (newTitle == null) return;

        String newDesc = JOptionPane.showInputDialog("New Description:", table.getValueAt(row, 2));
        if (newDesc == null) return;

        int newPriority = Integer.parseInt(
                JOptionPane.showInputDialog("New Priority:", table.getValueAt(row, 3))
        );

        String newTags = JOptionPane.showInputDialog(
                "New Tags (vd: oop,jdbc):", table.getValueAt(row, 4)
        );
        if (newTags == null) newTags = "";

        List<String> tagNames = List.of(newTags.split(","));

        dao.updateWithTags(new Task(id, newTitle, newDesc, newPriority), tagNames);
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
