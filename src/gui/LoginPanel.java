package gui;

import dao.UserDAO;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login panel for user authentication
 * Demonstrates GUI components, event handling, and user interaction
 */
public class LoginPanel extends JPanel {
    
    private TaskManagerApp parentApp;
    private UserDAO userDAO;
    
    // GUI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JCheckBox showPasswordCheckBox;
    
    /**
     * Constructor initializes the login panel
     * @param parentApp Reference to main application
     */
    public LoginPanel(TaskManagerApp parentApp) {
        this.parentApp = parentApp;
        this.userDAO = new UserDAO();
        
        setupPanel();
        createComponents();
        layoutComponents();
        addEventListeners();
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
        // Create text fields
        usernameField = TaskManagerApp.createStyledTextField();
        usernameField.setPreferredSize(new Dimension(300, 40));
        
        passwordField = TaskManagerApp.createStyledPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 40));
        
        // Create buttons
        loginButton = TaskManagerApp.createStyledButton(
            "Login", 
            TaskManagerApp.PRIMARY_COLOR, 
            Color.WHITE
        );
        loginButton.setPreferredSize(new Dimension(300, 45));
        
        registerButton = TaskManagerApp.createStyledButton(
            "Register New User", 
            TaskManagerApp.SECONDARY_COLOR, 
            Color.WHITE
        );
        registerButton.setPreferredSize(new Dimension(300, 45));
        
        // Create show password checkbox
        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setFont(TaskManagerApp.SMALL_FONT);
        showPasswordCheckBox.setBackground(Color.WHITE);
        showPasswordCheckBox.setForeground(TaskManagerApp.TEXT_SECONDARY);
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.DANGER_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    /**
     * Layouts all components using various layout managers
     */
    private void layoutComponents() {
        // Create main container
        JPanel mainContainer = new JPanel(new GridBagLayout());
        mainContainer.setBackground(TaskManagerApp.BACKGROUND_COLOR);
        
        // Create login card
        JPanel loginCard = TaskManagerApp.createStyledPanel(null);
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setPreferredSize(new Dimension(400, 500));
        
        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Task Manager");
        titleLabel.setFont(TaskManagerApp.TITLE_FONT);
        titleLabel.setForeground(TaskManagerApp.PRIMARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Internal Company System");
        subtitleLabel.setFont(TaskManagerApp.BODY_FONT);
        subtitleLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        
        titlePanel.add(titleLabel);
        
        // Form section
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(TaskManagerApp.BODY_FONT);
        usernameLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(TaskManagerApp.BODY_FONT);
        passwordLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        buttonPanel.add(loginButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(registerButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(statusLabel);
        
        // Add components to form panel
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(showPasswordCheckBox);
        formPanel.add(Box.createVerticalStrut(25));
        formPanel.add(buttonPanel);
        
        // Add components to login card
        loginCard.add(titlePanel);
        loginCard.add(Box.createVerticalStrut(10));
        
        JPanel subtitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        subtitlePanel.setBackground(Color.WHITE);
        subtitlePanel.add(subtitleLabel);
        loginCard.add(subtitlePanel);
        
        loginCard.add(formPanel);
        
        // Add login card to main container
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(50, 50, 50, 50);
        mainContainer.add(loginCard, gbc);
        
        // Add info panel
        JPanel infoPanel = createInfoPanel();
        gbc.gridx = 1;
        gbc.insets = new Insets(50, 0, 50, 50);
        mainContainer.add(infoPanel, gbc);
        
        add(mainContainer, BorderLayout.CENTER);
    }
    
    /**
     * Creates an information panel with default credentials
     * @return Information panel
     */
    private JPanel createInfoPanel() {
        JPanel infoPanel = TaskManagerApp.createStyledPanel("Default Credentials");
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(300, 200));
        
        JLabel infoTitle = new JLabel("For testing purposes:");
        infoTitle.setFont(TaskManagerApp.BODY_FONT);
        infoTitle.setForeground(TaskManagerApp.TEXT_PRIMARY);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel adminLabel = new JLabel("Admin Account:");
        adminLabel.setFont(TaskManagerApp.BODY_FONT.deriveFont(Font.BOLD));
        adminLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        adminLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel adminUser = new JLabel("Username: admin");
        adminUser.setFont(TaskManagerApp.SMALL_FONT);
        adminUser.setForeground(TaskManagerApp.TEXT_SECONDARY);
        adminUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel adminPass = new JLabel("Password: admin123");
        adminPass.setFont(TaskManagerApp.SMALL_FONT);
        adminPass.setForeground(TaskManagerApp.TEXT_SECONDARY);
        adminPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel noteLabel = new JLabel("Note: You can register new users");
        noteLabel.setFont(TaskManagerApp.SMALL_FONT);
        noteLabel.setForeground(TaskManagerApp.WARNING_COLOR.darker());
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(infoTitle);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(adminLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(adminUser);
        infoPanel.add(adminPass);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(noteLabel);
        
        return infoPanel;
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        // Login button action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Register button action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrationDialog();
            }
        });
        
        // Show password checkbox
        showPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPasswordCheckBox.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('*');
                }
            }
        });
        
        // Enter key listeners
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
    }
    
    /**
     * Performs user login authentication
     */
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate input
        if (username.isEmpty()) {
            showStatus("Please enter username", TaskManagerApp.DANGER_COLOR);
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showStatus("Please enter password", TaskManagerApp.DANGER_COLOR);
            passwordField.requestFocus();
            return;
        }
        
        // Disable login button during authentication
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        
        // Perform authentication in background thread
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.authenticate(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        showStatus("Login successful!", TaskManagerApp.SUCCESS_COLOR);
                        parentApp.showDashboard(user);
                    } else {
                        showStatus("Invalid username or password", TaskManagerApp.DANGER_COLOR);
                        passwordField.selectAll();
                        passwordField.requestFocus();
                    }
                } catch (Exception e) {
                    showStatus("Login failed: " + e.getMessage(), TaskManagerApp.DANGER_COLOR);
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Shows user registration dialog
     */
    private void showRegistrationDialog() {
        UserRegistrationDialog dialog = new UserRegistrationDialog(parentApp, userDAO);
        dialog.setVisible(true);
    }
    
    /**
     * Shows status message
     * @param message Status message
     * @param color Message color
     */
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        
        // Clear status after 5 seconds
        Timer timer = new Timer(5000, e -> statusLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Clears all input fields
     */
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        showPasswordCheckBox.setSelected(false);
        passwordField.setEchoChar('*');
        statusLabel.setText(" ");
    }
    
    /**
     * Sets focus to username field
     */
    public void focusUsernameField() {
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }
}