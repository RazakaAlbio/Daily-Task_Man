package gui;

import config.DatabaseConfig;
import dao.UserDAO;
import models.User;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application class for Task Manager
 * Demonstrates Swing GUI components and application architecture
 */
public class TaskManagerApp extends JFrame {
    
    private static final String APP_TITLE = "Task Manager - Internal Company System";
    private static final String VERSION = "v1.0";
    
    // Application colors - modern and professional
    public static final Color PRIMARY_COLOR = new Color(37, 99, 235);      // Blue
    public static final Color SECONDARY_COLOR = new Color(99, 102, 241);   // Indigo
    public static final Color SUCCESS_COLOR = new Color(34, 197, 94);      // Green
    public static final Color WARNING_COLOR = new Color(251, 191, 36);     // Yellow
    public static final Color DANGER_COLOR = new Color(239, 68, 68);       // Red
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252); // Light gray
    public static final Color CARD_COLOR = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);        // Dark gray
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);   // Medium gray
    public static final Color BORDER_COLOR = new Color(226, 232, 240);     // Light border
    public static final Color INFO_COLOR = new Color(59, 130, 246);        // Info blue
    
    // Application fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    private User currentUser;
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    /**
     * Constructor initializes the application
     */
    public TaskManagerApp() {
        initializeApplication();
        setupGUI();
        showLoginScreen();
    }
    
    /**
     * Initializes the application components
     */
    private void initializeApplication() {
        // Set system look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
        
        // Initialize database
        DatabaseConfig.initializeDatabase();
        
        // Create default admin user if no users exist
        createDefaultAdminUser();
    }
    
    /**
     * Sets up the main GUI components
     */
    private void setupGUI() {
        setTitle(APP_TITLE + " " + VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        
        // Set application icon
        setIconImage(createApplicationIcon());
        
        // Setup main panel with CardLayout for switching between screens
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Initialize panels
        loginPanel = new LoginPanel(this);
        
        // Add panels to card layout
        mainPanel.add(loginPanel, "LOGIN");
        
        add(mainPanel);
        
        // Add window listener for proper cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    /**
     * Creates a simple application icon
     * @return Application icon image
     */
    private Image createApplicationIcon() {
        // Create a simple icon using Graphics2D
        int size = 32;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background circle
        g2d.setColor(PRIMARY_COLOR);
        g2d.fillOval(2, 2, size - 4, size - 4);
        
        // Draw checkmark
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(8, 16, 14, 22);
        g2d.drawLine(14, 22, 24, 10);
        
        g2d.dispose();
        return icon;
    }
    
    /**
     * Creates default admin user if no users exist
     */
    private void createDefaultAdminUser() {
        UserDAO userDAO = new UserDAO();
        if (userDAO.count() == 0) {
            User admin = new User(
                "admin",
                "admin123",
                "admin@company.com",
                User.Role.ADMIN,
                "System Administrator"
            );
            
            if (userDAO.save(admin)) {
                System.out.println("Default admin user created:");
                System.out.println("Username: admin");
                System.out.println("Password: admin123");
            }
        }
    }
    
    /**
     * Shows the login screen
     */
    public void showLoginScreen() {
        currentUser = null;
        cardLayout.show(mainPanel, "LOGIN");
        loginPanel.clearFields();
        loginPanel.focusUsernameField();
    }
    
    /**
     * Shows the dashboard after successful login
     * @param user Logged in user
     */
    public void showDashboard(User user) {
        this.currentUser = user;
        
        // Create dashboard panel if not exists
        if (dashboardPanel == null) {
            dashboardPanel = new DashboardPanel(this, user);
            mainPanel.add(dashboardPanel, "DASHBOARD");
        } else {
            dashboardPanel.updateUser(user);
            dashboardPanel.refreshData();
        }
        
        cardLayout.show(mainPanel, "DASHBOARD");
        setTitle(APP_TITLE + " - " + user.getDisplayName() + " (" + user.getRole().getDisplayName() + ")");
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Clean up dashboard panel
            if (dashboardPanel != null) {
                mainPanel.remove(dashboardPanel);
                dashboardPanel = null;
            }
            
            setTitle(APP_TITLE + " " + VERSION);
            showLoginScreen();
        }
    }
    
    /**
     * Exits the application with proper cleanup
     */
    public void exitApplication() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit the application?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Close database connection
            DatabaseConfig.closeConnection();
            
            // Exit application
            System.exit(0);
        }
    }
    
    /**
     * Gets the current logged in user
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Utility method to create styled buttons
     * @param text Button text
     * @param backgroundColor Background color
     * @param textColor Text color
     * @return Styled JButton
     */
    public static JButton createStyledButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 3),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = backgroundColor;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 3),
                    BorderFactory.createEmptyBorder(15, 30, 15, 30)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 3),
                    BorderFactory.createEmptyBorder(15, 30, 15, 30)
                ));
            }
        });
        
        return button;
    }
    
    /**
     * Utility method to create styled panels with border
     * @param title Panel title
     * @return Styled JPanel
     */
    public static JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        if (title != null && !title.isEmpty()) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    title,
                    0,
                    0,
                    HEADING_FONT,
                    TEXT_PRIMARY
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
        }
        
        return panel;
    }
    
    /**
     * Utility method to create styled text fields
     * @return Styled JTextField
     */
    public static JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(BODY_FONT);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBackground(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(156, 163, 175), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        textField.setPreferredSize(new Dimension(300, 40));
        return textField;
    }
    
    /**
     * Utility method to create styled password fields
     * @return Styled JPasswordField
     */
    public static JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(BODY_FONT);
        passwordField.setForeground(TEXT_PRIMARY);
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(156, 163, 175), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        passwordField.setPreferredSize(new Dimension(300, 40));
        return passwordField;
    }
    
    /**
     * Main method - entry point of the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system properties for better GUI appearance
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Run GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                new TaskManagerApp().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                    null,
                    "Failed to start application: " + e.getMessage(),
                    "Application Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}