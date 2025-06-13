package gui;

import dao.*;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

/**
 * Dialog for editing user information
 * Demonstrates form handling, validation, and user management
 */
public class UserEditDialog extends JDialog {
    
    private User user;
    private UserDAO userDAO;
    private boolean updateSuccessful = false;
    
    // GUI Components
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JComboBox<User.Role> roleCombo;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{3,20}$"
    );
    
    /**
     * Constructor for editing user
     * @param parent Parent window
     * @param user User to edit
     * @param userDAO User data access object
     */
    public UserEditDialog(Window parent, User user, UserDAO userDAO) {
        super(parent, "Edit User", ModalityType.APPLICATION_MODAL);
        
        this.user = user;
        this.userDAO = userDAO;
        
        setupDialog();
        createComponents();
        layoutComponents();
        addEventListeners();
        populateFields();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the dialog properties
     */
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(TaskManagerApp.BACKGROUND_COLOR);
    }
    
    /**
     * Creates all GUI components
     */
    private void createComponents() {
        // Create input fields
        usernameField = TaskManagerApp.createStyledTextField();
        usernameField.setPreferredSize(new Dimension(250, 35));
        
        emailField = TaskManagerApp.createStyledTextField();
        emailField.setPreferredSize(new Dimension(250, 35));
        
        fullNameField = TaskManagerApp.createStyledTextField();
        fullNameField.setPreferredSize(new Dimension(250, 35));
        
        // Create role combo box
        roleCombo = new JComboBox<>(User.Role.values());
        roleCombo.setFont(TaskManagerApp.BODY_FONT);
        roleCombo.setPreferredSize(new Dimension(250, 35));
        
        // Create buttons
        saveButton = TaskManagerApp.createStyledButton(
            "Update User",
            TaskManagerApp.SUCCESS_COLOR,
            Color.WHITE
        );
        
        cancelButton = TaskManagerApp.createStyledButton(
            "Cancel",
            TaskManagerApp.SECONDARY_COLOR,
            TaskManagerApp.TEXT_PRIMARY
        );
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.DANGER_COLOR);
    }
    
    /**
     * Layouts all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Edit User Information");
        titleLabel.setFont(TaskManagerApp.HEADER_FONT);
        titleLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Username section
        JPanel usernamePanel = createFieldPanel("Username *:", usernameField);
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Email section
        JPanel emailPanel = createFieldPanel("Email *:", emailField);
        mainPanel.add(emailPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Full name section
        JPanel fullNamePanel = createFieldPanel("Full Name *:", fullNameField);
        mainPanel.add(fullNamePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Role section
        JPanel rolePanel = createFieldPanel("Role *:", roleCombo);
        mainPanel.add(rolePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Status label
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a field panel with label and component
     * @param labelText Label text
     * @param component Component
     * @return Field panel
     */
    private JPanel createFieldPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(TaskManagerApp.BODY_FONT);
        label.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        saveButton.addActionListener(e -> updateUser());
        cancelButton.addActionListener(e -> {
            updateSuccessful = false;
            dispose();
        });
        
        // Enter key on fields
        usernameField.addActionListener(e -> updateUser());
        emailField.addActionListener(e -> updateUser());
        fullNameField.addActionListener(e -> updateUser());
    }
    
    /**
     * Populates fields with existing user data
     */
    private void populateFields() {
        if (user != null) {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            fullNameField.setText(user.getFullName());
            roleCombo.setSelectedItem(user.getRole());
        }
    }
    
    /**
     * Updates the user
     */
    private void updateUser() {
        if (!validateForm()) {
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Check if username is already taken (by another user)
                String newUsername = usernameField.getText().trim();
                if (!newUsername.equals(user.getUsername())) {
                    User existingUser = userDAO.findByUsername(newUsername);
                    if (existingUser != null) {
                        throw new Exception("Username '" + newUsername + "' is already taken");
                    }
                }
                
                // Check if email is already taken (by another user)
                String newEmail = emailField.getText().trim();
                if (!newEmail.equals(user.getEmail())) {
                    User existingUser = userDAO.findByEmail(newEmail);
                    if (existingUser != null) {
                        throw new Exception("Email '" + newEmail + "' is already taken");
                    }
                }
                
                // Update user properties
                user.setUsername(newUsername);
                user.setEmail(newEmail);
                user.setFullName(fullNameField.getText().trim());
                user.setRole((User.Role) roleCombo.getSelectedItem());
                
                // Save to database
                return userDAO.save(user);
            }
            
            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        updateSuccessful = true;
                        dispose();
                    } else {
                        showStatusMessage("Failed to update user");
                    }
                } catch (Exception e) {
                    showStatusMessage(e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Validates the form data
     * @return True if form is valid
     */
    private boolean validateForm() {
        // Clear previous status
        clearStatusMessage();
        
        // Validate username
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            showStatusMessage("Username is required");
            usernameField.requestFocus();
            return false;
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showStatusMessage("Username must be 3-20 characters long and contain only letters, numbers, and underscores");
            usernameField.requestFocus();
            return false;
        }
        
        // Validate email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showStatusMessage("Email is required");
            emailField.requestFocus();
            return false;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showStatusMessage("Please enter a valid email address");
            emailField.requestFocus();
            return false;
        }
        
        // Validate full name
        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            showStatusMessage("Full name is required");
            fullNameField.requestFocus();
            return false;
        }
        
        if (fullName.length() < 2 || fullName.length() > 100) {
            showStatusMessage("Full name must be between 2 and 100 characters");
            fullNameField.requestFocus();
            return false;
        }
        
        // Validate role
        if (roleCombo.getSelectedItem() == null) {
            showStatusMessage("Please select a role");
            roleCombo.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Shows status message
     * @param message Status message
     */
    private void showStatusMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(TaskManagerApp.DANGER_COLOR);
    }
    
    /**
     * Clears status message
     */
    private void clearStatusMessage() {
        statusLabel.setText(" ");
    }
    
    /**
     * Checks if update was successful
     * @return True if update was successful
     */
    public boolean isUpdateSuccessful() {
        return updateSuccessful;
    }
}