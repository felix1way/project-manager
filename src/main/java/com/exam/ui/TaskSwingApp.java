    package com.exam.ui;

    import com.exam.dao.TaskDAO;
    import com.exam.model.Task;

    import javax.swing.*;
    import javax.swing.table.DefaultTableCellRenderer;
    import javax.swing.table.DefaultTableModel;
    import java.awt.*;
    import java.time.LocalDate;
    import java.util.List;

    public class TaskSwingApp extends JFrame {

        private TaskDAO dao = new TaskDAO();
        private JTable table;
        private DefaultTableModel model;

        private JTextField txtSearch;
    //    private JTextField txtTagSearch;
        private JComboBox<String> cbTags;


        private void loadData() {
            model.setRowCount(0);
            List<Task> list = dao.getAllTasks();
            System.out.println(dao.getAllTasks().size());
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
                        t.getId(), t.getTitle(), t.getDescription(),
                        t.getPriority(), t.tagAsString()
                });
            }
        }

    //    private void searchByTag() {
    //        String tagKey = txtTagSearch.getText().trim();
    //        if (tagKey.isEmpty()) {
    //            loadData();
    //            return;
    //        }
    //
    //        model.setRowCount(0);
    //        List<Task> list = dao.searchByTagWithTags(tagKey);
    //        for (Task t : list) {
    //            model.addRow(new Object[]{
    //                    t.getId(), t.getTitle(), t.getDescription(),
    //                    t.getPriority(), t.tagsAsString()
    //            });
    //        }
    //    }

        private void searchByTagCombo(String tag) {
            if (tag == null || tag.equals("All")) {
                loadData();
                return;
            }

            model.setRowCount(0);
            List<Task> list = dao.searchByTagWithTags(tag);
            for (Task t : list) {
                model.addRow(new Object[]{
                        t.getId(), t.getTitle(), t.getDescription(),
                        t.getPriority(), t.tagAsString()
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
                        t.getId(), t.getTitle(), t.getDescription(),
                        t.getPriority(), t.tagAsString()
                });
            }
        }

        private void addTask() {
            String title = JOptionPane.showInputDialog("Title:");
            if (title == null) return;

            String desc = JOptionPane.showInputDialog("Description:");
            if (desc == null) return;

            int pr = Integer.parseInt(JOptionPane.showInputDialog("Priority:"));

            String tagInput = JOptionPane.showInputDialog("Tags (vd: oop,jdbc,...):");
            if (tagInput == null) tagInput = "";

            List<String> tagNames = List.of(tagInput.split(","));

            String deadlineStr = JOptionPane.showInputDialog("Deadline (yyyy-MM-dd):");
            LocalDate deadline = (deadlineStr == null || deadlineStr.isEmpty()) ? null : LocalDate.parse(deadlineStr);

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
            if (newTitle == null) return;

            String newDesc = JOptionPane.showInputDialog("New Description:", table.getValueAt(row, 2));
            if (newDesc == null) return;

            int newPriority = Integer.parseInt(
                    JOptionPane.showInputDialog("New Priority:", table.getValueAt(row, 3))
            );

            String newTags = JOptionPane.showInputDialog(
                    "New Tags (vd: oop,jdbc,...):", table.getValueAt(row, 4)
            );
            if (newTags == null) newTags = "";

            List<String> tagNames = List.of(newTags.split(","));

            String deadlineStr = JOptionPane.showInputDialog("New Deadline (yyyy-MM-dd):", table.getValueAt(row, 5));
            LocalDate newDeadline = (deadlineStr == null || deadlineStr.isEmpty()) ? null : LocalDate.parse(deadlineStr);

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

            model = new DefaultTableModel(new Object[]{"ID", "Title", "Description", "Priority", "Tags", "Deadline"}, 0);
            table = new JTable(model);
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value,
                                                               boolean isSelected,
                                                               boolean hasFocus,
                                                               int row,
                                                               int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


                    if (column == 5 && value != null) {
                        LocalDate deadline = (LocalDate) value;
                        if (deadline.isBefore(LocalDate.now())) {
                            c.setForeground(Color.RED);
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } else {
                        c.setForeground(Color.BLACK);
                    }

                    return c;
                }
            });
            loadData();

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            txtSearch = new JTextField(15);
            JButton btnSearch = new JButton("Search");
            JButton btnClear = new JButton("Clear");

    //      btnSearch.addActionListener(e -> searchTasks());
            btnClear.addActionListener(e -> {
                txtSearch.setText("");
                cbTags.setSelectedItem("All"); // reset tag filter
                loadData();
            });


            topPanel.add(new JLabel("Keyword:"));
            topPanel.add(txtSearch);
            topPanel.add(btnSearch);
            topPanel.add(btnClear);

    //        txtTagSearch = new JTextField(10);
    //        JButton btnSearchTag = new JButton("Search Tag");
    //        btnSearchTag.addActionListener(e -> searchByTag());
    //
    //        topPanel.add(new JLabel("Tag (text):"));
    //        topPanel.add(txtTagSearch);
    //        topPanel.add(btnSearchTag);

            cbTags = new JComboBox<>();
            loadTagsToComboBox(cbTags);

            cbTags.addActionListener(e -> {
                String tag = (String) cbTags.getSelectedItem();
                searchByTagCombo(tag);
            });

            topPanel.add(new JLabel("Tag (select):"));
            topPanel.add(cbTags);

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
            JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnAddTag = new JButton("Add Tag");
            JButton btnDeleteTag = new JButton("Delete Tag");

            btnAddTag.addActionListener(e -> {
                String tagName = JOptionPane.showInputDialog("Enter new tag name:");
                if (tagName != null && !tagName.trim().isEmpty()) {
                    dao.insertTag(tagName.trim());
                    loadTagsToComboBox(cbTags);
                }
            });

            btnDeleteTag.addActionListener(e -> {
                String tagName = (String) cbTags.getSelectedItem();
                if (tagName != null && !tagName.equals("All")) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete tag '" + tagName + "'?",
                            "Confirm Delete", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        dao.deleteTag(tagName);
                        loadTagsToComboBox(cbTags);
                        loadData();
                    }
                }
            });

            tagPanel.add(btnAddTag);
            tagPanel.add(btnDeleteTag);

            JPanel northPanel = new JPanel(new BorderLayout());
            northPanel.add(topPanel, BorderLayout.NORTH);
            northPanel.add(tagPanel, BorderLayout.SOUTH);

            add(northPanel, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        public static void main(String[] args) {
            new TaskSwingApp().setVisible(true);
        }

//        public static void main(String[] args) {
//            TaskDAO dao = new TaskDAO();
//            var list = dao.getAllWithTags();
//            System.out.println("TASK COUNT = " + list.size());
//            for (var t : list) System.out.println(t.getTitle());
//        }

    }
