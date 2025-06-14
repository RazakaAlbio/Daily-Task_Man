package gui;

import dao.*;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Main dashboard panel after user login
 * Demonstrates complex GUI layout, data display, and user interaction
 */
public class DashboardPanel extends JPanel {
    
    private TaskManagerApp parentApp;
    private User currentUser;
    
    // DAO objects
    private UserDAO userDAO;
    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    
    // GUI Components
    private JLabel welcomeLabel;
    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JButton logoutButton;
    private JButton refreshButton;
    
    // Navigation buttons
    private JButton dashboardButton;
    private JButton projectsButton;
    private JButton tasksButton;
    private JButton usersButton;
    
    // Current view
    private String currentView = "dashboard";
    
    /**
     * Constructor initializes the dashboard panel
     * @param parentApp Reference to main application
     * @param currentUser Currently logged in user
     */
    public DashboardPanel(TaskManagerApp parentApp, User currentUser) {
        this.parentApp = parentApp;
        this.currentUser = currentUser;
        
        // Initialize DAOs
        this.userDAO = new UserDAO();
        this.projectDAO = new ProjectDAO();
        this.taskDAO = new TaskDAO();
        
        setupPanel();
        createComponents();
        layoutComponents();
        addEventListeners();
        
        // Load initial view
        showDashboardView();
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
        // Welcome label
        welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setFont(TaskManagerApp.HEADER_FONT);
        welcomeLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        // Logout button
        logoutButton = TaskManagerApp.createStyledButton(
            "Logout", 
            TaskManagerApp.DANGER_COLOR, 
            Color.WHITE
        );
        logoutButton.setPreferredSize(new Dimension(120, 40));
        logoutButton.setFont(TaskManagerApp.BODY_FONT.deriveFont(Font.BOLD));
        
        // Refresh button
        refreshButton = TaskManagerApp.createStyledButton(
            "Refresh", 
            TaskManagerApp.SECONDARY_COLOR, 
            Color.WHITE
        );
        refreshButton.setPreferredSize(new Dimension(120, 40));
        refreshButton.setFont(TaskManagerApp.BODY_FONT.deriveFont(Font.BOLD));
        
        // Navigation buttons
        dashboardButton = createNavButton("Dashboard", "dashboard");
        projectsButton = createNavButton("Projects", "projects");
        tasksButton = createNavButton("Tasks", "tasks");
        usersButton = createNavButton("Users", "users");
        
        // Content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // Sidebar panel
        sidebarPanel = createSidebarPanel();
    }
    
    /**
     * Creates a navigation button
     * @param text Button text
     * @param view View identifier
     * @return Navigation button
     */
    private JButton createNavButton(String text, String view) {
        JButton button = new JButton(text);
        button.setFont(TaskManagerApp.BODY_FONT.deriveFont(Font.BOLD, 16f));
        button.setForeground(TaskManagerApp.TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, TaskManagerApp.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!currentView.equals(view)) {
                    button.setBackground(TaskManagerApp.BACKGROUND_COLOR);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!currentView.equals(view)) {
                    button.setBackground(Color.WHITE);
                }
            }
        });
        
        return button;
    }
    
    /**
     * Creates the sidebar panel
     * @return Sidebar panel
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, TaskManagerApp.BORDER_COLOR));
        sidebar.setPreferredSize(new Dimension(250, 0));
        
        // Logo/Title section
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(TaskManagerApp.PRIMARY_COLOR);
        logoPanel.setPreferredSize(new Dimension(250, 100));
        logoPanel.setMaximumSize(new Dimension(250, 100));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        
        JLabel logoLabel = new JLabel("Task Manager", SwingConstants.CENTER);
        logoLabel.setFont(TaskManagerApp.HEADER_FONT.deriveFont(Font.BOLD, 22f));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel, BorderLayout.CENTER);
        
        // Navigation section
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        
        // Add navigation buttons
        dashboardButton.setMaximumSize(new Dimension(250, 60));
        projectsButton.setMaximumSize(new Dimension(250, 60));
        tasksButton.setMaximumSize(new Dimension(250, 60));
        
        navPanel.add(dashboardButton);
        navPanel.add(projectsButton);
        navPanel.add(tasksButton);
        
        // Only show users button for admin/manager
        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
            usersButton.setMaximumSize(new Dimension(250, 60));
            navPanel.add(usersButton);
        }
        
        // Add components to sidebar
        sidebar.add(logoPanel);
        sidebar.add(navPanel);
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }
    
    /**
     * Layouts all components
     */
    private void layoutComponents() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, TaskManagerApp.BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Header buttons panel
        JPanel headerButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        headerButtonsPanel.setBackground(Color.WHITE);
        headerButtonsPanel.add(refreshButton);
        headerButtonsPanel.add(logoutButton);
        
        headerPanel.add(headerButtonsPanel, BorderLayout.EAST);
        
        // Main content area
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(TaskManagerApp.BACKGROUND_COLOR);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        // Logout button
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentApp.logout();
            }
        });
        
        // Refresh button
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCurrentView();
            }
        });
        
        // Navigation buttons
        dashboardButton.addActionListener(e -> showDashboardView());
        projectsButton.addActionListener(e -> showProjectsView());
        tasksButton.addActionListener(e -> showTasksView());
        
        if (currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER) {
            usersButton.addActionListener(e -> showUsersView());
        }
    }
    
    /**
     * Shows the dashboard view
     */
    private void showDashboardView() {
        currentView = "dashboard";
        updateNavigationButtons();
        
        SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                return new DashboardOverviewPanel(currentUser, projectDAO, taskDAO, userDAO);
            }
            
            @Override
            protected void done() {
                try {
                    contentPanel.removeAll();
                    contentPanel.add(get(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception e) {
                    showErrorMessage("Failed to load dashboard: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows the projects view
     */
    public void showProjectsView() {
        currentView = "projects";
        updateNavigationButtons();
        
        SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                return new ProjectManagementPanel(currentUser, projectDAO, taskDAO, userDAO);
            }
            
            @Override
            protected void done() {
                try {
                    contentPanel.removeAll();
                    contentPanel.add(get(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception e) {
                    showErrorMessage("Failed to load projects: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows the tasks view
     */
    public void showTasksView() {
        currentView = "tasks";
        updateNavigationButtons();
        
        SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                return new TaskManagementPanel(currentUser, taskDAO, projectDAO, userDAO);
            }
            
            @Override
            protected void done() {
                try {
                    contentPanel.removeAll();
                    contentPanel.add(get(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception e) {
                    showErrorMessage("Failed to load tasks: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows the users view (admin/manager only)
     */
    public void showUsersView() {
        if (currentUser.getRole() != User.Role.ADMIN && currentUser.getRole() != User.Role.MANAGER) {
            showErrorMessage("Access denied: Insufficient permissions");
            return;
        }
        
        currentView = "users";
        updateNavigationButtons();
        
        SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                return new UserManagementPanel(currentUser, userDAO);
            }
            
            @Override
            protected void done() {
                try {
                    contentPanel.removeAll();
                    contentPanel.add(get(), BorderLayout.CENTER);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                } catch (Exception e) {
                    showErrorMessage("Failed to load users: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Updates navigation button states
     */
    private void updateNavigationButtons() {
        // Reset all buttons
        dashboardButton.setBackground(Color.WHITE);
        projectsButton.setBackground(Color.WHITE);
        tasksButton.setBackground(Color.WHITE);
        usersButton.setBackground(Color.WHITE);
        
        // Highlight current view
        switch (currentView) {
            case "dashboard":
                dashboardButton.setBackground(TaskManagerApp.PRIMARY_COLOR.brighter());
                break;
            case "projects":
                projectsButton.setBackground(TaskManagerApp.PRIMARY_COLOR.brighter());
                break;
            case "tasks":
                tasksButton.setBackground(TaskManagerApp.PRIMARY_COLOR.brighter());
                break;
            case "users":
                usersButton.setBackground(TaskManagerApp.PRIMARY_COLOR.brighter());
                break;
        }
    }
    
    /**
     * Refreshes the current view
     */
    private void refreshCurrentView() {
        switch (currentView) {
            case "dashboard":
                showDashboardView();
                break;
            case "projects":
                showProjectsView();
                break;
            case "tasks":
                showTasksView();
                break;
            case "users":
                showUsersView();
                break;
        }
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
     * Gets the current user
     * @return Current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Updates the current user
     * @param user New current user
     */
    public void updateUser(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
    }
    
    /**
     * Refreshes all data in the dashboard
     */
    public void refreshData() {
        showDashboardView();
    }
    
    /**
     * Updates the welcome message for the current user
     */
    private void updateWelcomeMessage() {
        if (welcomeLabel != null && currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getDisplayName() + "!");
        }
    }
}