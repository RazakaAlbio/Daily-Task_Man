package gui;

import dao.*;
import models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Task management panel for viewing and managing tasks
 * Demonstrates complex table operations, filtering, and task assignment
 */
public class TaskManagementPanel extends JPanel {
    
    private User currentUser;
    private TaskDAO taskDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    
    // GUI Components
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton assignButton;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> priorityFilter;
    private JComboBox<String> assigneeFilter;
    private JLabel statusLabel;
    
    // Pagination components
    private JButton firstPageButton;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JButton lastPageButton;
    private JLabel pageInfoLabel;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalItems = 0;
    private List<Task> allTasks = new java.util.ArrayList<>();
    
    // Table columns
    private final String[] columnNames = {
        "ID", "Title", "Description", "Status", "Priority", "Project", 
        "Assigned To", "Assigner", "Due Date", "Created", "Updated"
    };
    
    /**
     * Constructor initializes the task management panel
     * @param currentUser Currently logged in user
     * @param taskDAO Task data access object
     * @param projectDAO Project data access object
     * @param userDAO User data access object
     */
    public TaskManagementPanel(User currentUser, TaskDAO taskDAO, 
                              ProjectDAO projectDAO, UserDAO userDAO) {
        this.currentUser = currentUser;
        this.taskDAO = taskDAO;
        this.projectDAO = projectDAO;
        this.userDAO = userDAO;
        
        setupPanel();
        createComponents();
        layoutComponents();
        addEventListeners();
        loadTasks();
    }
    
    /**
     * Sets up the panel properties
     */
    private void setupPanel() {
        setLayout(new BorderLayout());
        setBackground(TaskManagerApp.BACKGROUND_COLOR);
    }
    
    /**
     * Creates all GUI components
     */
    private void createComponents() {
        // Create table
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        taskTable = new JTable(tableModel);
        taskTable.setFont(TaskManagerApp.BODY_FONT);
        taskTable.setRowHeight(40);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setGridColor(TaskManagerApp.BORDER_COLOR);
        taskTable.setShowGrid(true);
        
        // Set column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Title
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Description
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Priority
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Project
        taskTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Assigned To
        taskTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Assigner
        taskTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Due Date
        taskTable.getColumnModel().getColumn(9).setPreferredWidth(120); // Created
        taskTable.getColumnModel().getColumn(10).setPreferredWidth(120); // Updated
        
        // Custom cell renderers
        taskTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        taskTable.getColumnModel().getColumn(4).setCellRenderer(new PriorityCellRenderer());
        
        // Create buttons
        createButton = TaskManagerApp.createStyledButton(
            "Create Task", 
            TaskManagerApp.SUCCESS_COLOR, 
            Color.WHITE
        );
        
        editButton = TaskManagerApp.createStyledButton(
            "Edit Task", 
            TaskManagerApp.WARNING_COLOR, 
            Color.WHITE
        );
        editButton.setEnabled(false);
        
        deleteButton = TaskManagerApp.createStyledButton(
            "Delete Task", 
            TaskManagerApp.DANGER_COLOR, 
            Color.WHITE
        );
        deleteButton.setEnabled(false);
        
        assignButton = TaskManagerApp.createStyledButton(
            "Assign Task", 
            TaskManagerApp.INFO_COLOR, 
            Color.WHITE
        );
        assignButton.setEnabled(false);
        
        // Create search field
        searchField = TaskManagerApp.createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 35));
        
        // Create filter combo boxes
        String[] statusOptions = {"All Status", "TODO", "IN_PROGRESS", "DONE"};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(TaskManagerApp.BODY_FONT);
        statusFilter.setPreferredSize(new Dimension(120, 35));
        
        String[] priorityOptions = {"All Priority", "LOW", "MEDIUM", "HIGH", "URGENT"};
        priorityFilter = new JComboBox<>(priorityOptions);
        priorityFilter.setFont(TaskManagerApp.BODY_FONT);
        priorityFilter.setPreferredSize(new Dimension(120, 35));
        
        assigneeFilter = new JComboBox<>();
        assigneeFilter.setFont(TaskManagerApp.BODY_FONT);
        assigneeFilter.setPreferredSize(new Dimension(150, 35));
        loadAssigneeFilter();
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        
        // Create pagination components
        firstPageButton = TaskManagerApp.createStyledButton("<<", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        firstPageButton.setPreferredSize(new Dimension(80, 40));
        
        prevPageButton = TaskManagerApp.createStyledButton("<", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        prevPageButton.setPreferredSize(new Dimension(80, 40));
        
        nextPageButton = TaskManagerApp.createStyledButton(">", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        nextPageButton.setPreferredSize(new Dimension(80, 40));
        
        lastPageButton = TaskManagerApp.createStyledButton(">>", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        lastPageButton.setPreferredSize(new Dimension(80, 40));
        
        pageInfoLabel = new JLabel("Page 1 of 1");
        pageInfoLabel.setFont(TaskManagerApp.BODY_FONT);
        pageInfoLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        updatePaginationButtons();
    }
    
    /**
     * Loads assignee filter options
     */
    private void loadAssigneeFilter() {
        try {
            assigneeFilter.addItem("All Assignees");
            assigneeFilter.addItem("Unassigned");
            assigneeFilter.addItem("My Tasks");
            
            List<User> users = userDAO.findAll();
            for (User user : users) {
                assigneeFilter.addItem(user.getFullName());
            }
        } catch (Exception e) {
            // Handle error silently
        }
    }
    
    /**
     * Layouts all components
     */
    private void layoutComponents() {
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Task Management");
        titleLabel.setFont(TaskManagerApp.TITLE_FONT);
        titleLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(statusLabel, BorderLayout.EAST);
        
        // Toolbar panel
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbarPanel.setBackground(Color.WHITE);
        toolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TaskManagerApp.BORDER_COLOR));
        
        // Search section
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(TaskManagerApp.BODY_FONT);
        searchLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        // Filter sections
        JLabel statusFilterLabel = new JLabel("Status:");
        statusFilterLabel.setFont(TaskManagerApp.BODY_FONT);
        statusFilterLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        JLabel priorityFilterLabel = new JLabel("Priority:");
        priorityFilterLabel.setFont(TaskManagerApp.BODY_FONT);
        priorityFilterLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        JLabel assigneeFilterLabel = new JLabel("Assignee:");
        assigneeFilterLabel.setFont(TaskManagerApp.BODY_FONT);
        assigneeFilterLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        toolbarPanel.add(searchLabel);
        toolbarPanel.add(searchField);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(statusFilterLabel);
        toolbarPanel.add(statusFilter);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(priorityFilterLabel);
        toolbarPanel.add(priorityFilter);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(assigneeFilterLabel);
        toolbarPanel.add(assigneeFilter);
        
        // Only show create button for admin/manager
        if (canCreateTasks()) {
            toolbarPanel.add(Box.createHorizontalStrut(20));
            toolbarPanel.add(createButton);
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Only show edit/delete buttons for admin/manager
        if (canEditTasks()) {
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        }
        
        // Show assign button for admin/manager
        if (canAssignTasks()) {
            buttonPanel.add(assignButton);
        }
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        paginationPanel.setBackground(Color.WHITE);
        paginationPanel.add(firstPageButton);
        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(nextPageButton);
        paginationPanel.add(lastPageButton);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(paginationPanel, BorderLayout.SOUTH);
        
        // Main layout
        add(titlePanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        // Table selection listener
        taskTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = taskTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection && canEditTasks());
                deleteButton.setEnabled(hasSelection && canEditTasks());
                assignButton.setEnabled(hasSelection && canAssignTasks());
            }
        });
        
        // Table double-click listener
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && taskTable.getSelectedRow() != -1) {
                    if (canEditTasks()) {
                        editTask();
                    } else {
                        viewTaskDetails();
                    }
                }
            }
        });
        
        // Button listeners
        if (canCreateTasks()) {
            createButton.addActionListener(e -> createTask());
        }
        
        if (canEditTasks()) {
            editButton.addActionListener(e -> editTask());
            deleteButton.addActionListener(e -> deleteTask());
        }
        
        if (canAssignTasks()) {
            assignButton.addActionListener(e -> assignTask());
        }
        
        // Filter listeners
        searchField.addActionListener(e -> {
            currentPage = 1;
            filterTasks();
        });
        statusFilter.addActionListener(e -> {
            currentPage = 1;
            filterTasks();
        });
        priorityFilter.addActionListener(e -> {
            currentPage = 1;
            filterTasks();
        });
        assigneeFilter.addActionListener(e -> {
            currentPage = 1;
            filterTasks();
        });
        
        // Pagination listeners
        firstPageButton.addActionListener(e -> {
            currentPage = 1;
            updateTableWithPagination();
        });
        
        prevPageButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableWithPagination();
            }
        });
        
        nextPageButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            if (currentPage < totalPages) {
                currentPage++;
                updateTableWithPagination();
            }
        });
        
        lastPageButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            currentPage = Math.max(1, totalPages);
            updateTableWithPagination();
        });
    }
    
    /**
     * Loads tasks into the table
     */
    private void loadTasks() {
        SwingWorker<List<Task>, Void> worker = new SwingWorker<List<Task>, Void>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                // Load tasks based on user role
                if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
                    return taskDAO.findAll();
                } else {
                    // Regular users see only their assigned tasks
                    return taskDAO.findByAssignedUser(currentUser.getId());
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Task> tasks = get();
                    updateTable(tasks);
                    updateStatusLabel(tasks.size() + " tasks loaded");
                } catch (Exception e) {
                    showErrorMessage("Failed to load tasks: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Updates the table with task data
     * @param tasks List of tasks
     */
    private void updateTable(List<Task> tasks) {
        this.allTasks = new java.util.ArrayList<>(tasks);
        this.totalItems = tasks.size();
        this.currentPage = 1;
        updateTableWithPagination();
    }
    
    /**
     * Updates the table with pagination
     */
    private void updateTableWithPagination() {
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allTasks.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Task task = allTasks.get(i);
            Object[] rowData = {
                task.getId(),
                task.getTitle(),
                truncateText(task.getDescription(), 50),
                task.getStatus(),
                task.getPriority(),
                task.getProject() != null ? task.getProject().getName() : "No Project",
                task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : "Unassigned",
                task.getAssignedBy() != null ? task.getAssignedBy().getFullName() : "Unknown",
                task.getDueDate() != null ? task.getDueDate().format(dateFormatter) : "No Due Date",
                task.getCreatedAt().format(formatter),
                task.getUpdatedAt().format(formatter)
            };
            
            tableModel.addRow(rowData);
        }
        
        updatePaginationInfo();
        updatePaginationButtons();
    }
    
    /**
     * Updates pagination information
     */
    private void updatePaginationInfo() {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        pageInfoLabel.setText(String.format("Page %d of %d (%d items)", currentPage, totalPages, totalItems));
    }
    
    /**
     * Updates pagination button states
     */
    private void updatePaginationButtons() {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        
        firstPageButton.setEnabled(currentPage > 1);
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < totalPages);
        lastPageButton.setEnabled(currentPage < totalPages);
    }
    
    /**
     * Filters tasks based on search and filter criteria
     */
    private void filterTasks() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusText = (String) statusFilter.getSelectedItem();
        String priorityText = (String) priorityFilter.getSelectedItem();
        String assigneeText = (String) assigneeFilter.getSelectedItem();
        
        SwingWorker<List<Task>, Void> worker = new SwingWorker<List<Task>, Void>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                List<Task> tasks;
                
                // Load tasks based on user role
                if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
                    tasks = taskDAO.findAll();
                } else {
                    tasks = taskDAO.findByAssignedUser(currentUser.getId());
                }
                
                return tasks.stream()
                    .filter(task -> {
                        // Search filter
                        if (!searchText.isEmpty()) {
                            return task.getTitle().toLowerCase().contains(searchText) ||
                                   task.getDescription().toLowerCase().contains(searchText);
                        }
                        return true;
                    })
                    .filter(task -> {
                        // Status filter
                        if (!"All Status".equals(statusText)) {
                            return task.getStatus().toString().equals(statusText);
                        }
                        return true;
                    })
                    .filter(task -> {
                        // Priority filter
                        if (!"All Priority".equals(priorityText)) {
                            return task.getPriority().toString().equals(priorityText);
                        }
                        return true;
                    })
                    .filter(task -> {
                        // Assignee filter
                        if ("Unassigned".equals(assigneeText)) {
                            return task.getAssignedUser() == null;
                        } else if ("My Tasks".equals(assigneeText)) {
                            return task.getAssignedUser() != null && 
                                   task.getAssignedUser().getId() == currentUser.getId();
                        } else if (!"All Assignees".equals(assigneeText)) {
                            return task.getAssignedUser() != null && 
                                   task.getAssignedUser().getFullName().equals(assigneeText);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            }
            
            @Override
            protected void done() {
                try {
                    List<Task> filteredTasks = get();
                    updateTable(filteredTasks);
                    updateStatusLabel(filteredTasks.size() + " tasks found");
                } catch (Exception e) {
                    showErrorMessage("Failed to filter tasks: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Creates a new task
     */
    private void createTask() {
        TaskDialog dialog = new TaskDialog(SwingUtilities.getWindowAncestor(this), 
                                         "Create Task", null, currentUser, projectDAO, userDAO);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Task newTask = dialog.getTask();
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return taskDAO.save(newTask);
                }
                
                @Override
                protected void done() {
                    try {
                        Boolean success = get();
                        if (success) {
                            updateStatusLabel("Task created successfully");
                            loadTasks();
                        } else {
                            showErrorMessage("Failed to create task");
                        }
                    } catch (Exception e) {
                        showErrorMessage("Failed to create task: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Edits the selected task
     */
    private void editTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Integer taskId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Task, Void> worker = new SwingWorker<Task, Void>() {
            @Override
            protected Task doInBackground() throws Exception {
                return taskDAO.findById(taskId);
            }
            
            @Override
            protected void done() {
                try {
                    Task task = get();
                    if (task != null) {
                        TaskDialog dialog = new TaskDialog(SwingUtilities.getWindowAncestor(TaskManagementPanel.this), 
                                                         "Edit Task", task, currentUser, projectDAO, userDAO);
                        dialog.setVisible(true);
                        
                        if (dialog.isConfirmed()) {
                            Task updatedTask = dialog.getTask();
                            updateTask(updatedTask);
                        }
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to load task: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Updates a task
     * @param task Task to update
     */
    private void updateTask(Task task) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return taskDAO.save(task);
            }
            
            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        updateStatusLabel("Task updated successfully");
                        loadTasks();
                    } else {
                        showErrorMessage("Failed to update task");
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to update task: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Deletes the selected task
     */
    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String taskTitle = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Create custom icon to prevent clipping
        ImageIcon icon = null;
        try {
            ImageIcon originalIcon = (ImageIcon) UIManager.getIcon("OptionPane.warningIcon");
            if (originalIcon != null) {
                // Scale the icon to a smaller size to prevent clipping
                Image img = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception e) {
            // Fallback to no icon if there's an issue
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete task '" + taskTitle + "'?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            icon
        );
        
        if (result == JOptionPane.YES_OPTION) {
            Integer taskId = (Integer) tableModel.getValueAt(selectedRow, 0);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return taskDAO.deleteById(taskId);
                }
                
                @Override
                protected void done() {
                    try {
                        Boolean success = get();
                        if (success) {
                            updateStatusLabel("Task deleted successfully");
                            loadTasks();
                        } else {
                            showErrorMessage("Failed to delete task");
                        }
                    } catch (Exception e) {
                        showErrorMessage("Failed to delete task: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Assigns the selected task to a user
     */
    private void assignTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Integer taskId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String taskTitle = (String) tableModel.getValueAt(selectedRow, 1);
        
        SwingWorker<Task, Void> worker = new SwingWorker<Task, Void>() {
            @Override
            protected Task doInBackground() throws Exception {
                return taskDAO.findById(taskId);
            }
            
            @Override
            protected void done() {
                try {
                    Task task = get();
                    if (task != null) {
                        showTaskAssignmentDialog(task);
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to load task: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows task assignment dialog
     * @param task Task to assign
     */
    private void showTaskAssignmentDialog(Task task) {
        // Get list of users for assignment
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userDAO.findAll();
            }
            
            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    User[] userArray = users.toArray(new User[0]);
                    
                    User selectedUser = (User) JOptionPane.showInputDialog(
                        TaskManagementPanel.this,
                        "Select user to assign task '" + task.getTitle() + "' to:",
                        "Assign Task",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        userArray,
                        task.getAssignedUser()
                    );
                    
                    if (selectedUser != null) {
                        assignTaskToUser(task, selectedUser);
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to load users: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Assigns task to selected user
     * @param task Task to assign
     * @param user User to assign to
     */
    private void assignTaskToUser(Task task, User user) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                task.setAssignedUser(user);
                task.setAssigner(currentUser);
                return taskDAO.save(task);
            }
            
            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        updateStatusLabel("Task assigned successfully to " + user.getFullName());
                        loadTasks();
                    } else {
                        showErrorMessage("Failed to assign task");
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to assign task: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Views task details (for regular users)
     */
    private void viewTaskDetails() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Integer taskId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Task, Void> worker = new SwingWorker<Task, Void>() {
            @Override
            protected Task doInBackground() throws Exception {
                return taskDAO.findById(taskId);
            }
            
            @Override
            protected void done() {
                try {
                    Task task = get();
                    if (task != null) {
                        showTaskDetailsDialog(task);
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to load task details: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows task details in a dialog
     * @param task Task to show details for
     */
    private void showTaskDetailsDialog(Task task) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Task Details", true);
        dialog.setSize(650, 550);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        detailsPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(new JLabel(task.getTitle()), gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        detailsPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea descArea = new JTextArea(task.getDescription() != null ? task.getDescription() : "No description");
        descArea.setEditable(false);
        descArea.setRows(12);
        descArea.setColumns(50);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(descArea.getFont().deriveFont(14f));
        descArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(500, 200));
        descScroll.setMinimumSize(new Dimension(400, 150));
        detailsPanel.add(descScroll, gbc);
        
        // Reset constraints for other fields
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Status
        gbc.gridx = 0; gbc.gridy = 2;
        detailsPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(new JLabel(task.getStatus().toString()), gbc);
        
        // Priority
        gbc.gridx = 0; gbc.gridy = 3;
        detailsPanel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(new JLabel(task.getPriority().toString()), gbc);
        
        // Project
        gbc.gridx = 0; gbc.gridy = 4;
        detailsPanel.add(new JLabel("Project:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(new JLabel(task.getProject() != null ? task.getProject().getName() : "No project"), gbc);
        
        // Assigned User
        gbc.gridx = 0; gbc.gridy = 5;
        detailsPanel.add(new JLabel("Assigned To:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(new JLabel(task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : "Unassigned"), gbc);
        
        // Assigner
        gbc.gridx = 0; gbc.gridy = 6;
        detailsPanel.add(new JLabel("Assigned By:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(new JLabel(task.getAssigner() != null ? task.getAssigner().getFullName() : "Unknown"), gbc);
        
        // Due Date
        gbc.gridx = 0; gbc.gridy = 7;
        detailsPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        detailsPanel.add(new JLabel(task.getDueDate() != null ? task.getDueDate().format(formatter) : "No due date"), gbc);
        
        // Created At
        gbc.gridx = 0; gbc.gridy = 8;
        detailsPanel.add(new JLabel("Created:"), gbc);
        gbc.gridx = 1;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        detailsPanel.add(new JLabel(task.getCreatedAt().format(dateTimeFormatter)), gbc);
        
        // Updated At
        gbc.gridx = 0; gbc.gridy = 9;
        detailsPanel.add(new JLabel("Updated:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(new JLabel(task.getUpdatedAt().format(dateTimeFormatter)), gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Add Edit button for employees if they are assigned to this task
        if (currentUser.getRole() == User.Role.EMPLOYEE && 
            task.getAssignedUser() != null && 
            task.getAssignedUser().getId() == currentUser.getId()) {
            JButton editButton = new JButton("Edit Progress");
            editButton.setBackground(new Color(0, 100, 200)); // Darker blue for better contrast
            editButton.setForeground(Color.BLACK);
            editButton.setOpaque(true);
            editButton.setBorderPainted(true);
            editButton.setBorder(BorderFactory.createRaisedBevelBorder());
            editButton.setFont(TaskManagerApp.BODY_FONT.deriveFont(Font.BOLD, 12f));
            editButton.setPreferredSize(new Dimension(120, 30));
            editButton.addActionListener(e -> {
                dialog.dispose();
                showEditTaskDialog(task);
            });
            buttonPanel.add(editButton);
        }
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    /**
     * Checks if current user can create tasks
     * @return True if user can create tasks
     */
    private boolean canCreateTasks() {
        return currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER;
    }
    
    /**
     * Checks if current user can edit tasks
     * @return True if user can edit tasks
     */
    private boolean canEditTasks() {
        return currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER;
    }
    
    /**
     * Checks if current user can assign tasks
     * @return True if user can assign tasks
     */
    private boolean canAssignTasks() {
        return currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER;
    }
    
    /**
     * Truncates text to specified length
     * @param text Text to truncate
     * @param maxLength Maximum length
     * @return Truncated text
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Updates the status label
     * @param message Status message
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
    }
    
    /**
     * Shows error message
     * @param message Error message
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Custom cell renderer for status column
     */
    private class StatusCellRenderer extends JLabel implements TableCellRenderer {
        
        public StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(TaskManagerApp.SMALL_FONT.deriveFont(Font.BOLD));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setText(value.toString());
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(Color.WHITE);
                
                // Set color based on status
                if (value instanceof Task.Status) {
                    Task.Status status = (Task.Status) value;
                    switch (status) {
                        case TODO:
                            setForeground(TaskManagerApp.WARNING_COLOR);
                            break;
                        case IN_PROGRESS:
                            setForeground(TaskManagerApp.INFO_COLOR);
                            break;
                        case DONE:
                            setForeground(TaskManagerApp.SUCCESS_COLOR);
                            break;
                        default:
                            setForeground(TaskManagerApp.TEXT_PRIMARY);
                    }
                } else {
                    setForeground(TaskManagerApp.TEXT_PRIMARY);
                }
            }
            
            return this;
        }
    }
    
    /**
     * Custom cell renderer for priority column
     */
    private class PriorityCellRenderer extends JLabel implements TableCellRenderer {
        
        public PriorityCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(TaskManagerApp.SMALL_FONT.deriveFont(Font.BOLD));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setText(value.toString());
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(Color.WHITE);
                
                // Set color based on priority
                if (value instanceof Task.Priority) {
                    Task.Priority priority = (Task.Priority) value;
                    switch (priority) {
                        case LOW:
                            setForeground(TaskManagerApp.SUCCESS_COLOR);
                            break;
                        case MEDIUM:
                            setForeground(TaskManagerApp.INFO_COLOR);
                            break;
                        case HIGH:
                            setForeground(TaskManagerApp.WARNING_COLOR);
                            break;
                        case URGENT:
                            setForeground(TaskManagerApp.DANGER_COLOR);
                            break;
                        default:
                            setForeground(TaskManagerApp.TEXT_PRIMARY);
                    }
                } else {
                    setForeground(TaskManagerApp.TEXT_PRIMARY);
                }
            }
            
            return this;
        }
    }
    
    /**
     * Shows edit task dialog for employees to update task progress
     * @param task Task to edit
     */
    private void showEditTaskDialog(Task task) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Task Progress", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Task title (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Task:"), gbc);
        gbc.gridx = 1;
        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(titleLabel, gbc);
        
        // Status dropdown
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        JComboBox<Task.Status> statusCombo = new JComboBox<>(Task.Status.values());
        statusCombo.setSelectedItem(task.getStatus());
        formPanel.add(statusCombo, gbc);
        
        // Progress notes
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Progress Notes:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea notesArea = new JTextArea(5, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setText(task.getDescription() != null ? task.getDescription() : "");
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(notesScroll, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save Progress");
        saveButton.setBackground(TaskManagerApp.SUCCESS_COLOR);
        saveButton.setForeground(Color.BLACK);
        saveButton.addActionListener(e -> {
            try {
                // Update task status and description
                task.setStatus((Task.Status) statusCombo.getSelectedItem());
                task.setDescription(notesArea.getText().trim());
                
                // Save to database
                taskDAO.save(task);
                
                // Refresh table
                loadTasks();
                
                // Show success message
                JOptionPane.showMessageDialog(dialog, 
                    "Task progress updated successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error updating task: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}