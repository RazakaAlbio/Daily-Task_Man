package gui;

import dao.UserDAO;
import dao.ProjectDAO;
import models.Project;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for creating and editing projects
 * Demonstrates dialog forms, validation, and data binding
 */
public class ProjectDialog extends JDialog {
    
    private Project project;
    private User currentUser;
    private UserDAO userDAO;
    private ProjectDAO projectDAO;
    private boolean confirmed = false;
    
    // GUI Components
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JComboBox<Project.Status> statusComboBox;
    private JComboBox<User> creatorComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    /**
     * Constructor for creating/editing projects
     * @param parent Parent window
     * @param title Dialog title
     * @param project Project to edit (null for new project)
     * @param currentUser Currently logged in user
     * @param userDAO User data access object
     */
    public ProjectDialog(Window parent, String title, Project project, 
                        User currentUser, UserDAO userDAO) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        
        this.project = project;
        this.currentUser = currentUser;
        this.userDAO = userDAO;
        this.projectDAO = new ProjectDAO();
        
        setupDialog();
        createComponents();
        layoutComponents();
        addEventListeners();
        populateFields();
    }
    
    /**
     * Sets up the dialog properties
     */
    private void setupDialog() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }
    
    /**
     * Creates all GUI components
     */
    private void createComponents() {
        // Create text fields
        nameField = TaskManagerApp.createStyledTextField();
        nameField.setPreferredSize(new Dimension(400, 40));
        
        // Create description area
        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setFont(TaskManagerApp.BODY_FONT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TaskManagerApp.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Create status combo box
        statusComboBox = new JComboBox<>(Project.Status.values());
        statusComboBox.setFont(TaskManagerApp.BODY_FONT);
        statusComboBox.setBackground(Color.WHITE);
        statusComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TaskManagerApp.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        statusComboBox.setPreferredSize(new Dimension(400, 40));
        
        // Create creator combo box
        creatorComboBox = new JComboBox<>();
        creatorComboBox.setFont(TaskManagerApp.BODY_FONT);
        creatorComboBox.setBackground(Color.WHITE);
        creatorComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TaskManagerApp.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        creatorComboBox.setPreferredSize(new Dimension(400, 40));
        
        // Load users for creator combo box
        loadUsers();
        
        // Create buttons
        saveButton = TaskManagerApp.createStyledButton(
            project == null ? "Create" : "Update", 
            TaskManagerApp.SUCCESS_COLOR, 
            Color.WHITE
        );
        saveButton.setPreferredSize(new Dimension(120, 40));
        
        cancelButton = TaskManagerApp.createStyledButton(
            "Cancel", 
            TaskManagerApp.SECONDARY_COLOR, 
            Color.WHITE
        );
        cancelButton.setPreferredSize(new Dimension(120, 40));
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.DANGER_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    /**
     * Layouts all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Title
        JLabel titleLabel = new JLabel(project == null ? "Create New Project" : "Edit Project");
        titleLabel.setFont(TaskManagerApp.HEADER_FONT);
        titleLabel.setForeground(TaskManagerApp.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add form fields
        addFormField(formPanel, "Project Name: *", nameField);
        
        // Description field with scroll pane
        JLabel descLabel = new JLabel("Description: *");
        descLabel.setFont(TaskManagerApp.BODY_FONT);
        descLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setPreferredSize(new Dimension(400, 120));
        descScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        formPanel.add(descLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(descScrollPane);
        formPanel.add(Box.createVerticalStrut(15));
        
        addFormField(formPanel, "Status:", statusComboBox);
        
        // Only show creator field for admin users
        if (currentUser.getRole() == User.Role.ADMIN) {
            addFormField(formPanel, "Creator:", creatorComboBox);
        }
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Helper method to add form fields
     * @param parent Parent panel
     * @param labelText Label text
     * @param component Form component
     */
    private void addFormField(JPanel parent, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setFont(TaskManagerApp.BODY_FONT);
        label.setForeground(TaskManagerApp.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        parent.add(label);
        parent.add(Box.createVerticalStrut(5));
        parent.add(component);
        parent.add(Box.createVerticalStrut(15));
    }
    
    /**
     * Loads users for the creator combo box
     */
    private void loadUsers() {
        try {
            List<User> users = userDAO.findAll();
            
            if (currentUser.getRole() == User.Role.ADMIN) {
                // Admin can see all admin and manager users
                List<User> eligibleUsers = users.stream()
                    .filter(user -> user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.MANAGER)
                    .collect(Collectors.toList());
                
                for (User user : eligibleUsers) {
                    creatorComboBox.addItem(user);
                }
                
                // Set current user as default
                creatorComboBox.setSelectedItem(currentUser);
            } else {
                // Non-admin users can only create projects for themselves
                creatorComboBox.addItem(currentUser);
                creatorComboBox.setSelectedItem(currentUser);
                creatorComboBox.setEnabled(false); // Disable selection for non-admin
            }
            
        } catch (Exception e) {
            showStatus("Failed to load users: " + e.getMessage(), TaskManagerApp.DANGER_COLOR);
        }
    }
    
    /**
     * Populates fields with existing project data
     */
    private void populateFields() {
        if (project != null) {
            nameField.setText(project.getName());
            descriptionArea.setText(project.getDescription());
            statusComboBox.setSelectedItem(project.getStatus());
            
            if (project.getCreator() != null) {
                creatorComboBox.setSelectedItem(project.getCreator());
            }
        } else {
            // Set default values for new project
            statusComboBox.setSelectedItem(Project.Status.PLANNING);
        }
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        // Save button action
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("\n*** Save button clicked! ***");
                saveProject();
            }
        });
        
        // Cancel button action
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Enter key listener for name field
        nameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProject();
            }
        });
    }
    
    /**
     * Saves the project
     */
    private void saveProject() {
        System.out.println("\n=== saveProject() method called ===");
        
        // Validate form data
        String validationError = validateFormData();
        if (validationError != null) {
            System.out.println("Validation error: " + validationError);
            showStatus(validationError, TaskManagerApp.DANGER_COLOR);
            return;
        }
        
        System.out.println("Validation passed, proceeding with save...");
        
        // Disable save button during save
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");
        
        // Create or update project
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String name = nameField.getText().trim();
                    String description = descriptionArea.getText().trim();
                    Project.Status status = (Project.Status) statusComboBox.getSelectedItem();
                    User creator = (User) creatorComboBox.getSelectedItem();
                    
                    System.out.println("ProjectDialog Debug - Creating/Updating project:");
                    System.out.println("Name: " + name);
                    System.out.println("Description: " + description);
                    System.out.println("Status: " + status);
                    System.out.println("Creator: " + (creator != null ? creator.getUsername() : "null"));
                    System.out.println("Current User: " + currentUser.getUsername());
                    
                    if (project == null) {
                        // Create new project
                        project = new Project(name, description, creator != null ? creator : currentUser);
                        project.setStatus(status);
                        System.out.println("Creating new project with creator: " + project.getCreator().getUsername());
                    } else {
                        // Update existing project
                        project.setName(name);
                        project.setDescription(description);
                        project.setStatus(status);
                        
                        // Only update creator if user is admin
                        if (currentUser.getRole() == User.Role.ADMIN && creator != null) {
                            project.setCreator(creator);
                        }
                        
                        project.setUpdatedAt(LocalDateTime.now());
                        System.out.println("Updating existing project");
                    }
                    
                    // Validate project before saving
                    if (!project.isValid()) {
                        System.err.println("Project validation failed!");
                        return false;
                    }
                    
                    // Save to database
                    System.out.println("Attempting to save project to database...");
                    boolean result = projectDAO.save(project);
                    System.out.println("Save result: " + result);
                    return result;
                } catch (Exception e) {
                    System.err.println("Exception in doInBackground: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                try {
                    Boolean success = get(); // Check for exceptions
                    if (success) {
                        confirmed = true;
                        showStatus("Project saved successfully!", TaskManagerApp.SUCCESS_COLOR);
                        
                        // Close dialog after 1 second
                        Timer timer = new Timer(1000, e -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatus("Failed to save project to database", TaskManagerApp.DANGER_COLOR);
                    }
                    
                } catch (Exception e) {
                    String errorMsg = "Failed to save project: " + e.getMessage();
                    showStatus(errorMsg, TaskManagerApp.DANGER_COLOR);
                    System.err.println("ProjectDialog Error: " + errorMsg);
                    e.printStackTrace(); // For debugging
                    
                    // Show detailed error dialog for debugging
                    JOptionPane.showMessageDialog(ProjectDialog.this, 
                        "Error Details:\n" + e.getClass().getSimpleName() + ": " + e.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    saveButton.setEnabled(true);
                    saveButton.setText(project == null ? "Create" : "Update");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Validates form data
     * @return Error message or null if valid
     */
    private String validateFormData() {
        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        
        System.out.println("ProjectDialog Validation Debug:");
        System.out.println("Name: '" + name + "' (length: " + name.length() + ")");
        System.out.println("Description: '" + description + "' (length: " + description.length() + ")");
        System.out.println("Creator selected: " + (creatorComboBox.getSelectedItem() != null));
        if (creatorComboBox.getSelectedItem() != null) {
            System.out.println("Creator: " + ((User)creatorComboBox.getSelectedItem()).getUsername());
        }
        
        // Check required fields
        if (name.isEmpty()) {
            nameField.requestFocus();
            System.out.println("Validation failed: Project name is required");
            return "Project name is required";
        }
        
        if (description.isEmpty()) {
            descriptionArea.requestFocus();
            System.out.println("Validation failed: Project description is required");
            return "Project description is required";
        }
        
        // Validate name length
        if (name.length() < 3) {
            nameField.requestFocus();
            System.out.println("Validation failed: Project name must be at least 3 characters");
            return "Project name must be at least 3 characters";
        }
        
        if (name.length() > 100) {
            nameField.requestFocus();
            System.out.println("Validation failed: Project name must not exceed 100 characters");
            return "Project name must not exceed 100 characters";
        }
        
        // Validate description length
        if (description.length() < 10) {
            descriptionArea.requestFocus();
            System.out.println("Validation failed: Project description must be at least 10 characters");
            return "Project description must be at least 10 characters";
        }
        
        if (description.length() > 1000) {
            descriptionArea.requestFocus();
            System.out.println("Validation failed: Project description must not exceed 1000 characters");
            return "Project description must not exceed 1000 characters";
        }
        
        // Validate creator selection
        if (creatorComboBox.getSelectedItem() == null) {
            System.out.println("Validation failed: Please select a project creator");
            return "Please select a project creator";
        }
        
        System.out.println("All validations passed!");
        return null; // All validations passed
    }
    
    /**
     * Shows status message
     * @param message Status message
     * @param color Message color
     */
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
    
    /**
     * Gets the project
     * @return Project object
     */
    public Project getProject() {
        return project;
    }
    
    /**
     * Checks if dialog was confirmed
     * @return True if confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}