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
        String[] statusOptions = {"All Status", "PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD", "CANCELLED"};
        statusFilter = new JComboBox<>(statusOptions);
        statusFilter.setFont(TaskManagerApp.BODY_FONT);
        statusFilter.setPreferredSize(new Dimension(150, 35));
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
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
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
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
            createButton.addActionListener(e -> createProject());
            editButton.addActionListener(e -> editProject());
            deleteButton.addActionListener(e -> deleteProject());
        }
        
        viewTasksButton.addActionListener(e -> viewProjectTasks());
        
        // Search and filter listeners
        searchField.addActionListener(e -> filterProjects());
        statusFilter.addActionListener(e -> filterProjects());
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
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Project project : projects) {
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
                    .toList();
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
        ProjectDialog dialog = new ProjectDialog(SwingUtilities.getWindowAncestor(this), 
                                               "Create Project", null, currentUser, userDAO);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Project newProject = dialog.getProject();
            
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
        
        Long projectId = (Long) tableModel.getValueAt(selectedRow, 0);
        
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
                return projectDAO.update(project);
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
            Long projectId = (Long) tableModel.getValueAt(selectedRow, 0);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return projectDAO.delete(projectId);
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
        
        Long projectId = (Long) tableModel.getValueAt(selectedRow, 0);
        String projectName = (String) tableModel.getValueAt(selectedRow, 1);
        
        // TODO: Open project tasks dialog or navigate to tasks view with filter
        JOptionPane.showMessageDialog(
            this,
            "View tasks for project: " + projectName + "\nProject ID: " + projectId,
            "Project Tasks",
            JOptionPane.INFORMATION_MESSAGE
        );
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
                        case ON_HOLD:
                            setForeground(TaskManagerApp.SECONDARY_COLOR);
                            break;
                        case CANCELLED:
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
}