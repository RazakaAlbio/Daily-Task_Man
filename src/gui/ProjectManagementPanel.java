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
 * Project management panel for viewing and managing projects
 * Demonstrates table display, CRUD operations, and data management
 */
public class ProjectManagementPanel extends JPanel {
    
    private User currentUser;
    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    
    // GUI Components
    private JTable projectTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewTasksButton;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
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
    private List<Project> allProjects = new java.util.ArrayList<>();
    
    // Table columns
    private final String[] columnNames = {
        "ID", "Name", "Description", "Status", "Creator", "Tasks", "Progress", "Created", "Updated"
    };
    
    /**
     * Constructor initializes the project management panel
     * @param currentUser Currently logged in user
     * @param projectDAO Project data access object
     * @param taskDAO Task data access object
     * @param userDAO User data access object
     */
    public ProjectManagementPanel(User currentUser, ProjectDAO projectDAO, 
                                 TaskDAO taskDAO, UserDAO userDAO) {
        this.currentUser = currentUser;
        this.projectDAO = projectDAO;
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
        
        setupPanel();
        createComponents();
        layoutComponents();
        addEventListeners();
        loadProjects();
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
        
        projectTable = new JTable(tableModel);
        projectTable.setFont(TaskManagerApp.BODY_FONT);
        projectTable.setRowHeight(40);
        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.setGridColor(TaskManagerApp.BORDER_COLOR);
        projectTable.setShowGrid(true);
        
        // Set column widths
        projectTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        projectTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        projectTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Description
        projectTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        projectTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Creator
        projectTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Tasks
        projectTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Progress
        projectTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Created
        projectTable.getColumnModel().getColumn(8).setPreferredWidth(120); // Updated
        
        // Custom cell renderer for status column
        projectTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        
        // Create buttons
        createButton = TaskManagerApp.createStyledButton(
            "Create Project", 
            TaskManagerApp.SUCCESS_COLOR, 
            Color.WHITE
        );
        
        editButton = TaskManagerApp.createStyledButton(
            "Edit Project", 
            TaskManagerApp.WARNING_COLOR, 
            Color.WHITE
        );
        editButton.setEnabled(false);
        
        deleteButton = TaskManagerApp.createStyledButton(
            "Delete Project", 
            TaskManagerApp.DANGER_COLOR, 
            Color.WHITE
        );
        deleteButton.setEnabled(false);
        
        viewTasksButton = TaskManagerApp.createStyledButton(
            "View Tasks", 
            TaskManagerApp.INFO_COLOR, 
            Color.WHITE
        );
        viewTasksButton.setEnabled(false);
        
        // Create search field
        searchField = TaskManagerApp.createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 35));
        
        // Create status filter
        String[] statusOptions = {"All Status", "PLANNING", "ACTIVE", "IN_PROGRESS", "ON_HOLD", "COMPLETED", "PAUSED"};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(TaskManagerApp.BODY_FONT);
        statusFilter.setPreferredSize(new Dimension(150, 35));
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        
        // Create pagination components
        firstPageButton = TaskManagerApp.createStyledButton("<<", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        firstPageButton.setPreferredSize(new Dimension(50, 30));
        
        prevPageButton = TaskManagerApp.createStyledButton("<", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        prevPageButton.setPreferredSize(new Dimension(50, 30));
        
        nextPageButton = TaskManagerApp.createStyledButton(">", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        nextPageButton.setPreferredSize(new Dimension(50, 30));
        
        lastPageButton = TaskManagerApp.createStyledButton(">>", TaskManagerApp.SECONDARY_COLOR, Color.WHITE);
        lastPageButton.setPreferredSize(new Dimension(50, 30));
        
        pageInfoLabel = new JLabel("Page 1 of 1");
        pageInfoLabel.setFont(TaskManagerApp.BODY_FONT);
        pageInfoLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        updatePaginationButtons();
    }
    
    /**
     * Layouts all components
     */
    private void layoutComponents() {
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Project Management");
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
        
        // Filter section
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(TaskManagerApp.BODY_FONT);
        filterLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        toolbarPanel.add(searchLabel);
        toolbarPanel.add(searchField);
        toolbarPanel.add(Box.createHorizontalStrut(20));
        toolbarPanel.add(filterLabel);
        toolbarPanel.add(statusFilter);
        
        // Only show create button for admin/manager
        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
            toolbarPanel.add(Box.createHorizontalStrut(20));
            toolbarPanel.add(createButton);
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        buttonPanel.add(viewTasksButton);
        
        // Only show edit/delete buttons for admin/manager
        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);
        }
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(projectTable);
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
        projectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = projectTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection && canEditProjects());
                deleteButton.setEnabled(hasSelection && canEditProjects());
                viewTasksButton.setEnabled(hasSelection);
            }
        });
        
        // Table double-click listener
        projectTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && projectTable.getSelectedRow() != -1) {
                    viewProjectTasks();
                }
            }
        });
        
        // Button listeners
        if (canEditProjects()) {
            createButton.addActionListener(e -> {
                System.out.println("\n*** Create Project button clicked! ***");
                createProject();
            });
            editButton.addActionListener(e -> {
                System.out.println("\n*** Edit Project button clicked! ***");
                editProject();
            });
            deleteButton.addActionListener(e -> {
                System.out.println("\n*** Delete Project button clicked! ***");
                deleteProject();
            });
        }
        
        viewTasksButton.addActionListener(e -> viewProjectTasks());
        
        // Search and filter listeners
        searchField.addActionListener(e -> {
            currentPage = 1;
            filterProjects();
        });
        statusFilter.addActionListener(e -> {
            currentPage = 1;
            filterProjects();
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
     * Loads projects into the table
     */
    private void loadProjects() {
        SwingWorker<List<Project>, Void> worker = new SwingWorker<List<Project>, Void>() {
            @Override
            protected List<Project> doInBackground() throws Exception {
                return projectDAO.findAll();
            }
            
            @Override
            protected void done() {
                try {
                    List<Project> projects = get();
                    updateTable(projects);
                    updateStatusLabel(projects.size() + " projects loaded");
                } catch (Exception e) {
                    showErrorMessage("Failed to load projects: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Updates the table with project data
     * @param projects List of projects
     */
    private void updateTable(List<Project> projects) {
        this.allProjects = new java.util.ArrayList<>(projects);
        this.totalItems = projects.size();
        this.currentPage = 1;
        updateTableWithPagination();
    }
    
    /**
     * Updates the table with pagination
     */
    private void updateTableWithPagination() {
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allProjects.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Project project = allProjects.get(i);
            // Get task count for project
            int taskCount = 0;
            String progress = "0%";
            
            try {
                List<Task> projectTasks = taskDAO.findByProject(project.getId());
                taskCount = projectTasks.size();
                
                if (taskCount > 0) {
                    long completedTasks = projectTasks.stream()
                        .filter(task -> task.getStatus() == Task.Status.DONE)
                        .count();
                    progress = String.format("%.0f%%", (completedTasks * 100.0) / taskCount);
                }
            } catch (Exception e) {
                // Handle error silently
            }
            
            Object[] rowData = {
                project.getId(),
                project.getName(),
                truncateText(project.getDescription(), 50),
                project.getStatus(),
                project.getCreator() != null ? project.getCreator().getFullName() : "Unknown",
                taskCount,
                progress,
                project.getCreatedAt().format(formatter),
                project.getUpdatedAt().format(formatter)
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
     * Filters projects based on search and status criteria
     */
    private void filterProjects() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusText = (String) statusFilter.getSelectedItem();
        
        SwingWorker<List<Project>, Void> worker = new SwingWorker<List<Project>, Void>() {
            @Override
            protected List<Project> doInBackground() throws Exception {
                List<Project> projects = projectDAO.findAll();
                
                return projects.stream()
                    .filter(project -> {
                        // Search filter
                        if (!searchText.isEmpty()) {
                            return project.getName().toLowerCase().contains(searchText) ||
                                   project.getDescription().toLowerCase().contains(searchText);
                        }
                        return true;
                    })
                    .filter(project -> {
                        // Status filter
                        if (!"All Status".equals(statusText)) {
                            return project.getStatus().toString().equals(statusText);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            }
            
            @Override
            protected void done() {
                try {
                    List<Project> filteredProjects = get();
                    updateTable(filteredProjects);
                    updateStatusLabel(filteredProjects.size() + " projects found");
                } catch (Exception e) {
                    showErrorMessage("Failed to filter projects: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Creates a new project
     */
    private void createProject() {
        System.out.println("\n=== createProject() method called ===");
        System.out.println("Current user: " + (currentUser != null ? currentUser.getUsername() : "null"));
        
        ProjectDialog dialog = new ProjectDialog(SwingUtilities.getWindowAncestor(this), 
                                                "Create Project", null, currentUser, userDAO);
        System.out.println("ProjectDialog created, making it visible...");
        dialog.setVisible(true);
        System.out.println("Dialog closed, checking if confirmed...");
        
        if (dialog.isConfirmed()) {
            System.out.println("Dialog confirmed, starting save worker...");
            Project newProject = dialog.getProject();
            System.out.println("New project: " + (newProject != null ? newProject.getName() : "null"));
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return projectDAO.save(newProject);
                }
                
                @Override
                protected void done() {
                    try {
                        Boolean success = get();
                        if (success) {
                            updateStatusLabel("Project created successfully");
                            loadProjects();
                        } else {
                            showErrorMessage("Failed to create project");
                        }
                    } catch (Exception e) {
                        showErrorMessage("Failed to create project: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Edits the selected project
     */
    private void editProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Integer projectId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Project, Void> worker = new SwingWorker<Project, Void>() {
            @Override
            protected Project doInBackground() throws Exception {
                return projectDAO.findById(projectId);
            }
            
            @Override
            protected void done() {
                try {
                    Project project = get();
                    if (project != null) {
                        ProjectDialog dialog = new ProjectDialog(SwingUtilities.getWindowAncestor(ProjectManagementPanel.this), 
                                                               "Edit Project", project, currentUser, userDAO);
                        dialog.setVisible(true);
                        
                        if (dialog.isConfirmed()) {
                            Project updatedProject = dialog.getProject();
                            updateProject(updatedProject);
                        }
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to load project: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Updates a project
     * @param project Project to update
     */
    private void updateProject(Project project) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return projectDAO.save(project);
            }
            
            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        updateStatusLabel("Project updated successfully");
                        loadProjects();
                    } else {
                        showErrorMessage("Failed to update project");
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to update project: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Deletes the selected project
     */
    private void deleteProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String projectName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete project '" + projectName + "'?\n" +
            "This will also delete all associated tasks.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            Integer projectId = (Integer) tableModel.getValueAt(selectedRow, 0);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return projectDAO.deleteById(projectId);
                }
                
                @Override
                protected void done() {
                    try {
                        Boolean success = get();
                        if (success) {
                            updateStatusLabel("Project deleted successfully");
                            loadProjects();
                        } else {
                            showErrorMessage("Failed to delete project");
                        }
                    } catch (Exception e) {
                        showErrorMessage("Failed to delete project: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Views tasks for the selected project
     */
    private void viewProjectTasks() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Integer projectId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String projectName = (String) tableModel.getValueAt(selectedRow, 1);
        
        SwingWorker<List<Task>, Void> worker = new SwingWorker<List<Task>, Void>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                TaskDAO taskDAO = new TaskDAO();
                return taskDAO.findByProject(projectId);
            }
            
            @Override
            protected void done() {
                try {
                    List<Task> tasks = get();
                    showProjectTasksDialog(projectName, tasks);
                } catch (Exception e) {
                    showErrorMessage("Failed to load project tasks: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows project tasks in a dialog
     * @param projectName Name of the project
     * @param tasks List of tasks for the project
     */
    private void showProjectTasksDialog(String projectName, List<Task> tasks) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tasks for Project: " + projectName, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        // Create table model
        String[] columnNames = {"ID", "Title", "Status", "Priority", "Assigned To", "Due Date"};
        DefaultTableModel taskTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Populate table
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Task task : tasks) {
            Object[] rowData = {
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getPriority(),
                task.getAssignedUser() != null ? task.getAssignedUser().getFullName() : "Unassigned",
                task.getDueDate() != null ? task.getDueDate().format(formatter) : "No due date"
            };
            taskTableModel.addRow(rowData);
        }
        
        JTable taskTable = new JTable(taskTableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        taskTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        taskTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(taskTable);
        
        // Create info panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("Total tasks: " + tasks.size()));
        
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
        infoPanel.add(new JLabel("Completed: " + completedTasks));
        
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        infoPanel.add(new JLabel("In Progress: " + inProgressTasks));
        
        // Create close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        
        // Layout
        dialog.setLayout(new BorderLayout());
        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    /**
     * Checks if current user can edit projects
     * @return True if user can edit projects
     */
    private boolean canEditProjects() {
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
                if (value instanceof Project.Status) {
                    Project.Status status = (Project.Status) value;
                    switch (status) {
                        case PLANNING:
                            setForeground(TaskManagerApp.WARNING_COLOR);
                            break;
                        case IN_PROGRESS:
                            setForeground(TaskManagerApp.INFO_COLOR);
                            break;
                        case COMPLETED:
                            setForeground(TaskManagerApp.SUCCESS_COLOR);
                            break;
                        case PAUSED:
                            setForeground(TaskManagerApp.SECONDARY_COLOR);
                            break;
                        case ACTIVE:
                            setForeground(TaskManagerApp.PRIMARY_COLOR);
                            break;
                        default:
                            setForeground(TaskManagerApp.TEXT_PRIMARY);
                            break;
                    }
                } else {
                    setForeground(TaskManagerApp.TEXT_PRIMARY);
                }
            }
            
            return this;
        }
    }
}