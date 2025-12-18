package com.exam.ui;

import com.exam.dao.ITaskDAO;
import com.exam.dao.TaskDAO;
import com.exam.model.Task;
import com.exam.model.Tag;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskSwingApp extends JFrame {

    private final ITaskDAO dao;

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSearch;
    private JComboBox<String> cbTags;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnSearch;
    private JButton btnSortAsc;
    private JButton btnSortDesc;
    private JButton btnReload;

    public TaskSwingApp(ITaskDAO dao) {
        this.dao = dao;
        initUI();
        loadData();
    }

    private void initUI() {
        setTitle("Task Manager");
        setSize(1000, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"ID", "Title", "Description", "Priority", "Deadline", "Tag"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                c.setForeground(Color.BLACK);
                c.setBackground(Color.WHITE);

                Object deadlineObj = table.getValueAt(row, 4);
                if (deadlineObj instanceof LocalDate) {
                    LocalDate deadline = (LocalDate) deadlineObj;
                    if (deadline.isBefore(LocalDate.now())) {
                        c.setBackground(new Color(255, 200, 200)); // overdue
                    }
                }

                if (isSelected) {
                    c.setBackground(new Color(184, 207, 229));
                }

                return c;
            }
        });

        txtSearch = new JTextField(12);
        cbTags = new JComboBox<>();

        btnSearch = new JButton("Search");
        btnSortAsc = new JButton("Priority ↑");
        btnSortDesc = new JButton("Priority ↓");
        btnReload = new JButton("Reload");

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Keyword:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        searchPanel.add(new JLabel("Tag:"));
        searchPanel.add(cbTags);

        searchPanel.add(btnSortAsc);
        searchPanel.add(btnSortDesc);
        searchPanel.add(btnReload);

        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> showAddDialog());
        btnUpdate.addActionListener(e -> showUpdateDialog());
        btnDelete.addActionListener(e -> deleteSelectedTask());

        btnSearch.addActionListener(e -> searchByKeyword());
        cbTags.addActionListener(e -> searchByTag());
        btnSortAsc.addActionListener(e -> sortByPriority(true));
        btnSortDesc.addActionListener(e -> sortByPriority(false));
        btnReload.addActionListener(e -> loadData());
    }

    private void loadData() {
        List<Task> tasks = dao.getAllTasks();
        render(tasks);
        loadTags(tasks);
    }

    private void render(List<Task> tasks) {
        model.setRowCount(0);
        for (Task t : tasks) {
            model.addRow(new Object[]{t.getId(), t.getTitle(), t.getDescription(), t.getPriority(), t.getDeadline(), t.getTag() != null ? t.getTag().getName() : ""});
        }
    }

    private void loadTags(List<Task> tasks) {
        Set<String> tags = new HashSet<>();
        for (Task t : tasks) {
            if (t.getTag() != null) {
                tags.add(t.getTag().getName());
            }
        }

        cbTags.removeAllItems();
        cbTags.addItem("All");
        for (String tag : tags) {
            cbTags.addItem(tag);
        }
        cbTags.setSelectedIndex(0);
    }

    private void searchByKeyword() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        render(((TaskDAO) dao).search(keyword));
    }

    private void searchByTag() {
        String tag = (String) cbTags.getSelectedItem();
        if (tag == null || tag.equals("All")) {
            loadData();
            return;
        }

        render(dao.getAllTasks().stream().filter(t -> t.getTag() != null && t.getTag().getName().equals(tag)).toList());
    }

    private void sortByPriority(boolean asc) {
        render(((TaskDAO) dao).sortByPriority(asc));
    }

    private void showAddDialog() {
        JTextField txtTitle = new JTextField();
        JTextField txtDesc = new JTextField();
        JTextField txtPriority = new JTextField();
        JTextField txtDeadline = new JTextField();
        JTextField txtTag = new JTextField();

        Object[] fields = {"Title:", txtTitle, "Description:", txtDesc, "Priority (1-5):", txtPriority, "Deadline (yyyy-MM-dd):", txtDeadline, "Tag:", txtTag};

        int result = JOptionPane.showConfirmDialog(this, fields, "Add Task", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        Task task = validateAndBuildTask(null, txtTitle, txtDesc, txtPriority, txtDeadline, txtTag);
        if (task == null) return;

        dao.insert(task);
        loadData();
    }

    private void showUpdateDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Please select a task to update");
            return;
        }

        int id = (int) model.getValueAt(row, 0);

        JTextField txtTitle = new JTextField(model.getValueAt(row, 1).toString());
        JTextField txtDesc = new JTextField(model.getValueAt(row, 2).toString());
        JTextField txtPriority = new JTextField(model.getValueAt(row, 3).toString());
        JTextField txtDeadline = new JTextField(model.getValueAt(row, 4) != null ? model.getValueAt(row, 4).toString() : "");
        JTextField txtTag = new JTextField(model.getValueAt(row, 5) != null ? model.getValueAt(row, 5).toString() : "");

        Object[] fields = {"Title:", txtTitle, "Description:", txtDesc, "Priority (1-5):", txtPriority, "Deadline (yyyy-MM-dd):", txtDeadline, "Tag:", txtTag};

        int result = JOptionPane.showConfirmDialog(this, fields, "Update Task", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        Task task = validateAndBuildTask(id, txtTitle, txtDesc, txtPriority, txtDeadline, txtTag);
        if (task == null) return;

        dao.update(task);
        loadData();
    }

    private void deleteSelectedTask() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Please select a task to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this task?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        int id = (int) model.getValueAt(row, 0);
        dao.delete(id);
        loadData();
    }

    private Task validateAndBuildTask(Integer id, JTextField txtTitle, JTextField txtDesc, JTextField txtPriority, JTextField txtDeadline, JTextField txtTag) {
        String title = txtTitle.getText().trim();
        if (title.isEmpty()) {
            showError("Title must not be empty");
            return null;
        }

        int priority;
        try {
            priority = Integer.parseInt(txtPriority.getText().trim());
            if (priority < 1 || priority > 5) throw new Exception();
        } catch (Exception e) {
            showError("Priority must be between 1 and 5");
            return null;
        }

        LocalDate deadline = null;
        if (!txtDeadline.getText().trim().isEmpty()) {
            try {
                deadline = LocalDate.parse(txtDeadline.getText().trim());
            } catch (Exception e) {
                showError("Deadline must be yyyy-MM-dd");
                return null;
            }
        }

        Task task = new Task();
        if (id != null) task.setId(id);
        task.setTitle(title);
        task.setDescription(txtDesc.getText().trim());
        task.setPriority(priority);
        task.setDeadline(deadline);

        String tagName = txtTag.getText().trim();
        if (!tagName.isEmpty()) {
            task.setTag(new Tag(0, tagName));
        }

        return task;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ITaskDAO dao = new TaskDAO();
            new TaskSwingApp(dao).setVisible(true);
        });
    }
}
