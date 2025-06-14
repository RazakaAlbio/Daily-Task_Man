package gui;

import dao.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import models.*;

/**
 * Dashboard overview panel showing statistics and recent activities
 * Demonstrates data visualization and summary displays
 */
public class DashboardOverviewPanel extends JPanel {
    
    private User currentUser;
    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    
    /**
     * Constructor initializes the overview panel
     * @param currentUser Currently logged in user
     * @param projectDAO Project data access object
     * @param taskDAO Task data access object
     * @param userDAO User data access object
     */
    public DashboardOverviewPanel(User currentUser, ProjectDAO projectDAO, 
                                 TaskDAO taskDAO, UserDAO userDAO) {
        this.currentUser = currentUser;
        this.projectDAO = projectDAO;
        this.taskDAO = taskDAO;
        this.userDAO = userDAO;
        
        setupPanel();
        loadData();
    }
    
    /**
     * Sets up the panel properties
     */
    private void setupPanel() {
        setLayout(new BorderLayout());
        setBackground(TaskManagerApp.BACKGROUND_COLOR);
    }
    
    /**
     * Loads and displays dashboard data
     */
    private void loadData() {
        // Create main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(TaskManagerApp.BACKGROUND_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(TaskManagerApp.TITLE_FONT);
        titleLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        
        // Recent activities panel
        JPanel activitiesPanel = createRecentActivitiesPanel();
        
        // Quick actions panel
        JPanel quickActionsPanel = createQuickActionsPanel();
        
        // Layout components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(TaskManagerApp.BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel contentPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        contentPanel.setBackground(TaskManagerApp.BACKGROUND_COLOR);
        contentPanel.add(statsPanel);
        contentPanel.add(activitiesPanel);
        contentPanel.add(quickActionsPanel);
        
        mainContainer.add(topPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    /**
     * Creates the statistics panel
     * @return Statistics panel
     */
    private JPanel createStatisticsPanel() {
        JPanel panel = TaskManagerApp.createStyledPanel("Statistics");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(280, 450));
        panel.setMaximumSize(new Dimension(280, 450));
        
        try {
            // Get statistics based on user role
            if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
                addGlobalStatistics(panel);
            } else {
                addPersonalStatistics(panel);
            }
        } catch (Exception e) {
            addErrorMessage(panel, "Failed to load statistics: " + e.getMessage());
        }
        
        return panel;
    }
    
    /**
     * Adds global statistics for admin/manager
     * @param panel Panel to add statistics to
     */
    private void addGlobalStatistics(JPanel panel) {
        // Total projects
        List<Project> allProjects = projectDAO.findAll();
        addStatItem(panel, "Total Projects", String.valueOf(allProjects.size()), TaskManagerApp.PRIMARY_COLOR);
        
        // Active projects
        List<Project> activeProjects = projectDAO.findByStatus(Project.Status.IN_PROGRESS);
        addStatItem(panel, "Active Projects", String.valueOf(activeProjects.size()), TaskManagerApp.SUCCESS_COLOR);
        
        // Total tasks
        List<Task> allTasks = taskDAO.findAll();
        addStatItem(panel, "Total Tasks", String.valueOf(allTasks.size()), TaskManagerApp.SECONDARY_COLOR);
        
        // Pending tasks
        List<Task> pendingTasks = taskDAO.findByStatus(Task.Status.TODO);
        addStatItem(panel, "Pending Tasks", String.valueOf(pendingTasks.size()), TaskManagerApp.WARNING_COLOR);
        
        // Overdue tasks
        List<Task> overdueTasks = taskDAO.findOverdueTasks();
        addStatItem(panel, "Overdue Tasks", String.valueOf(overdueTasks.size()), TaskManagerApp.DANGER_COLOR);
        
        // Total users
        List<User> allUsers = userDAO.findAll();
        addStatItem(panel, "Total Users", String.valueOf(allUsers.size()), TaskManagerApp.INFO_COLOR);
    }
    
    /**
     * Adds personal statistics for regular users
     * @param panel Panel to add statistics to
     */
    private void addPersonalStatistics(JPanel panel) {
        // My tasks
        List<Task> myTasks = taskDAO.findByAssignedUser(currentUser.getId());
        addStatItem(panel, "My Tasks", String.valueOf(myTasks.size()), TaskManagerApp.PRIMARY_COLOR);
        
        // Completed tasks
        List<Task> completedTasks = taskDAO.findByAssignedUser(currentUser.getId())
            .stream()
            .filter(task -> task.getStatus() == Task.Status.DONE)
            .collect(Collectors.toList());
        addStatItem(panel, "Completed", String.valueOf(completedTasks.size()), TaskManagerApp.SUCCESS_COLOR);
        
        // In progress tasks
        List<Task> inProgressTasks = taskDAO.findByAssignedUser(currentUser.getId())
            .stream()
            .filter(task -> task.getStatus() == Task.Status.IN_PROGRESS)
            .collect(Collectors.toList());
        addStatItem(panel, "In Progress", String.valueOf(inProgressTasks.size()), TaskManagerApp.WARNING_COLOR);
        
        // Overdue tasks
        List<Task> overdueTasks = taskDAO.findOverdueTasks()
            .stream()
            .filter(task -> task.getAssignedUser() != null && task.getAssignedUser().getId() == currentUser.getId())
            .collect(Collectors.toList());
        addStatItem(panel, "Overdue", String.valueOf(overdueTasks.size()), TaskManagerApp.DANGER_COLOR);
        
        // Tasks due today
        List<Task> tasksDueToday = taskDAO.findTasksDueToday()
            .stream()
            .filter(task -> task.getAssignedUser() != null && task.getAssignedUser().getId() == currentUser.getId())
            .collect(Collectors.toList());
        addStatItem(panel, "Due Today", String.valueOf(tasksDueToday.size()), TaskManagerApp.INFO_COLOR);
    }
    
    /**
     * Adds a statistic item to the panel
     * @param panel Panel to add to
     * @param label Statistic label
     * @param value Statistic value
     * @param color Value color
     */
    private void addStatItem(JPanel panel, String label, String value, Color color) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(TaskManagerApp.BODY_FONT);
        labelComponent.setForeground(TaskManagerApp.TEXT_SECONDARY);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(TaskManagerApp.HEADER_FONT);
        valueComponent.setForeground(color);
        valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        
        itemPanel.add(labelComponent, BorderLayout.WEST);
        itemPanel.add(valueComponent, BorderLayout.EAST);
        
        panel.add(itemPanel);
        
        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(TaskManagerApp.BORDER_COLOR);
        panel.add(separator);
    }
    
    /**
     * Creates the recent activities panel
     * @return Recent activities panel
     */
    private JPanel createRecentActivitiesPanel() {
        JPanel panel = TaskManagerApp.createStyledPanel("Recent Activities");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(280, 450));
        panel.setMaximumSize(new Dimension(280, 450));
        
        try {
            // Get recent tasks
            List<Task> recentTasks;
            if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
                recentTasks = taskDAO.findAll().stream()
                    .sorted((t1, t2) -> t2.getUpdatedAt().compareTo(t1.getUpdatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
            } else {
                recentTasks = taskDAO.findByAssignedUser(currentUser.getId()).stream()
                    .sorted((t1, t2) -> t2.getUpdatedAt().compareTo(t1.getUpdatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
            }
            
            if (recentTasks.isEmpty()) {
                addInfoMessage(panel, "No recent activities");
            } else {
                for (Task task : recentTasks) {
                    addActivityItem(panel, task);
                }
            }
        } catch (Exception e) {
            addErrorMessage(panel, "Failed to load activities: " + e.getMessage());
        }
        
        return panel;
    }
    
    /**
     * Adds an activity item to the panel
     * @param panel Panel to add to
     * @param task Task to display
     */
    private void addActivityItem(JPanel panel, Task task) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(TaskManagerApp.BODY_FONT.deriveFont(Font.BOLD));
        titleLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel statusLabel = new JLabel("Status: " + task.getStatus());
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(getStatusColor(task.getStatus()));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String assigneeText = task.getAssignedUser() != null ? 
            task.getAssignedUser().getFullName() : "Unassigned";
        JLabel assigneeLabel = new JLabel("Assigned to: " + assigneeText);
        assigneeLabel.setFont(TaskManagerApp.SMALL_FONT);
        assigneeLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        assigneeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        itemPanel.add(titleLabel);
        itemPanel.add(statusLabel);
        itemPanel.add(assigneeLabel);
        
        panel.add(itemPanel);
        
        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(TaskManagerApp.BORDER_COLOR);
        panel.add(separator);
    }
    
    /**
     * Creates the quick actions panel
     * @return Quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = TaskManagerApp.createStyledPanel("Quick Actions");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(280, 450));
        panel.setMaximumSize(new Dimension(280, 450));
        
        // Create action buttons based on user role
        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
            addActionButton(panel, "Create New Project", TaskManagerApp.PRIMARY_COLOR, e -> {
                // TODO: Implement create project dialog
                JOptionPane.showMessageDialog(this, "Create Project feature coming soon!");
            });
            
            addActionButton(panel, "Create New Task", TaskManagerApp.SUCCESS_COLOR, e -> {
                // TODO: Implement create task dialog
                JOptionPane.showMessageDialog(this, "Create Task feature coming soon!");
            });
            
            addActionButton(panel, "View All Users", TaskManagerApp.INFO_COLOR, e -> {
                // TODO: Navigate to users view
                JOptionPane.showMessageDialog(this, "Navigate to Users view");
            });
        }
        
        addActionButton(panel, "View My Tasks", TaskManagerApp.SECONDARY_COLOR, e -> {
            // TODO: Navigate to tasks view
            JOptionPane.showMessageDialog(this, "Navigate to Tasks view");
        });
        
        addActionButton(panel, "View Projects", TaskManagerApp.WARNING_COLOR, e -> {
            // TODO: Navigate to projects view
            JOptionPane.showMessageDialog(this, "Navigate to Projects view");
        });
        
        return panel;
    }
    
    /**
     * Adds an action button to the panel
     * @param panel Panel to add to
     * @param text Button text
     * @param color Button color
     * @param action Button action
     */
    private void addActionButton(JPanel panel, String text, Color color, 
                                java.awt.event.ActionListener action) {
        JButton button = TaskManagerApp.createStyledButton(text, color, Color.WHITE);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(220, 35));
        button.setMaximumSize(new Dimension(220, 35));
        button.addActionListener(action);
        
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonWrapper.add(button);
        
        panel.add(buttonWrapper);
        panel.add(Box.createVerticalStrut(8));
    }
    
    /**
     * Adds an error message to the panel
     * @param panel Panel to add to
     * @param message Error message
     */
    private void addErrorMessage(JPanel panel, String message) {
        JLabel errorLabel = new JLabel(message);
        errorLabel.setFont(TaskManagerApp.BODY_FONT);
        errorLabel.setForeground(TaskManagerApp.DANGER_COLOR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(errorLabel);
    }
    
    /**
     * Adds an info message to the panel
     * @param panel Panel to add to
     * @param message Info message
     */
    private void addInfoMessage(JPanel panel, String message) {
        JLabel infoLabel = new JLabel(message);
        infoLabel.setFont(TaskManagerApp.BODY_FONT);
        infoLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(infoLabel);
    }
    
    /**
     * Gets color for task status
     * @param status Task status
     * @return Status color
     */
    private Color getStatusColor(Task.Status status) {
        switch (status) {
            case TODO:
                return TaskManagerApp.WARNING_COLOR;
            case IN_PROGRESS:
                return TaskManagerApp.INFO_COLOR;
            case DONE:
                return TaskManagerApp.SUCCESS_COLOR;
            default:
                return TaskManagerApp.TEXT_SECONDARY;
        }
    }
}