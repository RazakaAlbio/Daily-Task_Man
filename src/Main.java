import gui.TaskManagerApp;
import database.DatabaseManager;
import javax.swing.*;

/**
 * Main class - Entry point for the Task Manager application
 * Demonstrates application initialization and error handling
 */
public class Main {
    
    /**
     * Main method - Application entry point
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }
        
        // Set application properties
        System.setProperty("java.awt.headless", "false");
        
        // Run application on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize database
                System.out.println("Initializing Task Manager Application...");
                
                DatabaseManager dbManager = DatabaseManager.getInstance();
                
                // Test database connection
                if (!dbManager.testConnection()) {
                    showDatabaseError();
                    return;
                }
                
                // Create default admin user if needed
                try {
                    dbManager.createDefaultAdminUser();
                } catch (Exception e) {
                    System.err.println("Failed to create default admin user: " + e.getMessage());
                }
                
                // Start the application
                System.out.println("Starting Task Manager GUI...");
                TaskManagerApp app = new TaskManagerApp();
                app.setVisible(true);
                
                System.out.println("Task Manager Application started successfully!");
                
            } catch (Exception e) {
                System.err.println("Failed to start application: " + e.getMessage());
                e.printStackTrace();
                
                showApplicationError(e);
            }
        });
    }
    
    /**
     * Shows database connection error dialog
     */
    private static void showDatabaseError() {
        String message = "<html><body style='width: 300px'>" +
            "<h3>Database Connection Error</h3>" +
            "<p>Failed to connect to the MySQL database.</p>" +
            "<p><b>Please ensure:</b></p>" +
            "<ul>" +
            "<li>MySQL server is running (XAMPP)</li>" +
            "<li>Database 'task_manager' exists or can be created</li>" +
            "<li>MySQL JDBC driver is available</li>" +
            "<li>Connection settings are correct</li>" +
            "</ul>" +
            "<p><b>Default settings:</b></p>" +
            "<ul>" +
            "<li>Host: localhost:3306</li>" +
            "<li>Username: root</li>" +
            "<li>Password: (empty)</li>" +
            "</ul>" +
            "</body></html>";
        
        JOptionPane.showMessageDialog(
            null,
            message,
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
        
        System.exit(1);
    }
    
    /**
     * Shows general application error dialog
     * @param exception The exception that occurred
     */
    private static void showApplicationError(Exception exception) {
        String message = "<html><body style='width: 300px'>" +
            "<h3>Application Error</h3>" +
            "<p>An unexpected error occurred while starting the application.</p>" +
            "<p><b>Error:</b> " + exception.getMessage() + "</p>" +
            "<p>Please check the console for more details.</p>" +
            "</body></html>";
        
        JOptionPane.showMessageDialog(
            null,
            message,
            "Application Error",
            JOptionPane.ERROR_MESSAGE
        );
        
        System.exit(1);
    }
}