package gui;

import dao.UserDAO;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

/**
 * User registration dialog
 * Demonstrates dialog creation, form validation, and user creation
 */
public class UserRegistrationDialog extends JDialog {
    
    private UserDAO userDAO;
    private boolean registrationSuccessful = false;
    
    // GUI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JComboBox<User.Role> roleComboBox;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    /**
     * Constructor initializes the registration dialog
     * @param parent Parent frame
     * @param userDAO User data access object
     */
    public UserRegistrationDialog(Frame parent, UserDAO userDAO) {
        super(parent, "Register New User", true);
        this.userDAO = userDAO;
        
        setupDialog();
        createComponents();
        layoutComponents();
        addEventListeners();
    }
    
    /**
     * Sets up the dialog properties
     */
    private void setupDialog() {
        setSize(650, 850);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
    }
    
    /**
     * Creates all GUI components
     */
    private void createComponents() {
        // Create text fields
        usernameField = TaskManagerApp.createStyledTextField();
        passwordField = TaskManagerApp.createStyledPasswordField();
        confirmPasswordField = TaskManagerApp.createStyledPasswordField();
        emailField = TaskManagerApp.createStyledTextField();
        fullNameField = TaskManagerApp.createStyledTextField();
        
        // Create role combo box
        roleComboBox = new JComboBox<>(User.Role.values());
        roleComboBox.setFont(TaskManagerApp.BODY_FONT);
        roleComboBox.setForeground(TaskManagerApp.TEXT_PRIMARY);
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(156, 163, 175), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        roleComboBox.setPreferredSize(new Dimension(300, 40));
        
        // Create buttons
        registerButton = TaskManagerApp.createStyledButton(
            "REGISTER USER", 
            TaskManagerApp.SUCCESS_COLOR, 
            Color.WHITE
        );
        registerButton.setPreferredSize(new Dimension(220, 60));
        
        cancelButton = TaskManagerApp.createStyledButton(
            "CANCEL", 
            TaskManagerApp.DANGER_COLOR, 
            Color.WHITE
        );
        cancelButton.setPreferredSize(new Dimension(220, 60));
        
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
        JLabel titleLabel = new JLabel("Create New User Account");
        titleLabel.setFont(TaskManagerApp.HEADER_FONT);
        titleLabel.setForeground(TaskManagerApp.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add form fields
        addFormField(formPanel, "Full Name:", fullNameField);
        addFormField(formPanel, "Username:", usernameField);
        addFormField(formPanel, "Email:", emailField);
        addFormField(formPanel, "Password:", passwordField);
        addFormField(formPanel, "Confirm Password:", confirmPasswordField);
        addFormField(formPanel, "Role:", roleComboBox);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        // Add components to main panel
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(30));
        
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
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TaskManagerApp.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        parent.add(label);
        parent.add(Box.createVerticalStrut(8));
        parent.add(component);
        parent.add(Box.createVerticalStrut(18));
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        // Register button action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegistration();
            }
        });
        
        // Cancel button action
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * Performs user registration
     */
    private void performRegistration() {
        // Get form data
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        User.Role role = (User.Role) roleComboBox.getSelectedItem();
        
        // Validate form data
        String validationError = validateFormData(fullName, username, email, password, confirmPassword);
        if (validationError != null) {
            showStatus(validationError, TaskManagerApp.DANGER_COLOR);
            return;
        }
        
        // Disable register button during registration
        registerButton.setEnabled(false);
        registerButton.setText("Registering...");
        
        // Perform registration in background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Check if username already exists
                if (userDAO.findByUsername(username) != null) {
                    throw new Exception("Username already exists");
                }
                
                // Check if email already exists
                if (userDAO.findByEmail(email) != null) {
                    throw new Exception("Email already exists");
                }
                
                // Create new user
                User newUser = new User(username, password, email, role, fullName);
                return userDAO.save(newUser);
            }
            
            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        registrationSuccessful = true;
                        showStatus("User registered successfully!", TaskManagerApp.SUCCESS_COLOR);
                        
                        // Close dialog after 2 seconds
                        Timer timer = new Timer(2000, e -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatus("Registration failed", TaskManagerApp.DANGER_COLOR);
                    }
                } catch (Exception e) {
                    showStatus("Registration failed: " + e.getMessage(), TaskManagerApp.DANGER_COLOR);
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Validates form data
     * @param fullName Full name
     * @param username Username
     * @param email Email
     * @param password Password
     * @param confirmPassword Confirm password
     * @return Error message or null if valid
     */
    private String validateFormData(String fullName, String username, String email, 
                                   String password, String confirmPassword) {
        
        // Check required fields
        if (fullName.isEmpty()) {
            fullNameField.requestFocus();
            return "Full name is required";
        }
        
        if (username.isEmpty()) {
            usernameField.requestFocus();
            return "Username is required";
        }
        
        if (email.isEmpty()) {
            emailField.requestFocus();
            return "Email is required";
        }
        
        if (password.isEmpty()) {
            passwordField.requestFocus();
            return "Password is required";
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordField.requestFocus();
            return "Please confirm password";
        }
        
        // Validate username length and format
        if (username.length() < 3) {
            usernameField.requestFocus();
            return "Username must be at least 3 characters";
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameField.requestFocus();
            return "Username can only contain letters, numbers, and underscores";
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            emailField.requestFocus();
            return "Please enter a valid email address";
        }
        
        // Validate password strength
        if (password.length() < 6) {
            passwordField.requestFocus();
            return "Password must be at least 6 characters";
        }
        
        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.requestFocus();
            return "Passwords do not match";
        }
        
        // Validate full name
        if (fullName.length() < 2) {
            fullNameField.requestFocus();
            return "Full name must be at least 2 characters";
        }
        
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
     * Checks if registration was successful
     * @return true if registration was successful, false otherwise
     */
    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }
}