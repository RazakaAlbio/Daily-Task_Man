package database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Database manager class for handling database connections and initialization
 * Demonstrates singleton pattern and database connection management
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Connection connection;
    
    // Database configuration
    private String url;
    private String username;
    private String password;
    private String driver;
    
    /**
     * Private constructor for singleton pattern
     */
    private DatabaseManager() {
        loadDatabaseConfig();
        initializeDatabase();
    }
    
    /**
     * Gets the singleton instance of DatabaseManager
     * @return DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Loads database configuration from properties file or uses defaults
     */
    private void loadDatabaseConfig() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
                
                url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/task_manager");
                username = props.getProperty("db.username", "root");
                password = props.getProperty("db.password", "");
                driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            } else {
                // Use default configuration for XAMPP MySQL
                url = "jdbc:mysql://localhost:3306/task_manager?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                username = "root";
                password = "";
                driver = "com.mysql.cj.jdbc.Driver";
            }
        } catch (IOException e) {
            System.err.println("Failed to load database configuration, using defaults: " + e.getMessage());
            
            // Use default configuration for XAMPP MySQL
            url = "jdbc:mysql://localhost:3306/task_manager?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            username = "root";
            password = "";
            driver = "com.mysql.cj.jdbc.Driver";
        }
    }
    
    /**
     * Initializes the database connection and creates tables if they don't exist
     */
    private void initializeDatabase() {
        try {
            // Load JDBC driver
            Class.forName(driver);
            
            // Create connection
            connection = DriverManager.getConnection(url, username, password);
            
            System.out.println("Database connection established successfully");
            
            // Create tables if they don't exist
            createTables();
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            System.err.println("Please make sure mysql-connector-java is in your classpath");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            System.err.println("Please make sure MySQL server is running and the database exists");
        }
    }
    
    /**
     * Creates database tables if they don't exist
     */
    private void createTables() {
        try {
            Statement stmt = connection.createStatement();
            
            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password_hash VARCHAR(255) NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "full_name VARCHAR(100) NOT NULL," +
                "role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "created_by BIGINT," +
                "updated_by BIGINT," +
                "INDEX idx_username (username)," +
                "INDEX idx_email (email)," +
                "INDEX idx_role (role)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            stmt.executeUpdate(createUsersTable);
            
            // Create projects table
            String createProjectsTable = "CREATE TABLE IF NOT EXISTS projects (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(200) NOT NULL," +
                "description TEXT," +
                "status ENUM('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PLANNING'," +
                "creator_id BIGINT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "created_by BIGINT," +
                "updated_by BIGINT," +
                "FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT," +
                "INDEX idx_name (name)," +
                "INDEX idx_status (status)," +
                "INDEX idx_creator (creator_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            stmt.executeUpdate(createProjectsTable);
            
            // Create tasks table
            String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(200) NOT NULL," +
                "description TEXT," +
                "status ENUM('TODO', 'IN_PROGRESS', 'DONE') NOT NULL DEFAULT 'TODO'," +
                "priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM'," +
                "project_id BIGINT," +
                "assigned_user_id BIGINT," +
                "assigner_id BIGINT," +
                "due_date DATE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "created_by BIGINT," +
                "updated_by BIGINT," +
                "FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL," +
                "FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL," +
                "FOREIGN KEY (assigner_id) REFERENCES users(id) ON DELETE SET NULL," +
                "INDEX idx_title (title)," +
                "INDEX idx_status (status)," +
                "INDEX idx_priority (priority)," +
                "INDEX idx_project (project_id)," +
                "INDEX idx_assigned_user (assigned_user_id)," +
                "INDEX idx_due_date (due_date)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            stmt.executeUpdate(createTasksTable);
            
            // Create status_history table for tracking status changes
            String createStatusHistoryTable = "CREATE TABLE IF NOT EXISTS status_history (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "entity_type ENUM('PROJECT', 'TASK') NOT NULL," +
                "entity_id BIGINT NOT NULL," +
                "old_status VARCHAR(50)," +
                "new_status VARCHAR(50) NOT NULL," +
                "changed_by BIGINT," +
                "changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "notes TEXT," +
                "INDEX idx_entity (entity_type, entity_id)," +
                "INDEX idx_changed_at (changed_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            
            stmt.executeUpdate(createStatusHistoryTable);
            
            stmt.close();
            
            System.out.println("Database tables created/verified successfully");
            
        } catch (SQLException e) {
            System.err.println("Failed to create database tables: " + e.getMessage());
        }
    }
    
    /**
     * Gets the database connection
     * @return Database connection
     * @throws SQLException if connection is not available
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            // Reconnect if connection is lost
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
    
    /**
     * Tests the database connection
     * @return True if connection is successful
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            rs.close();
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Closes the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
        }
    }
    
    /**
     * Executes a SQL script for database initialization
     * @param script SQL script to execute
     * @throws SQLException if execution fails
     */
    public void executeScript(String script) throws SQLException {
        Statement stmt = getConnection().createStatement();
        
        // Split script by semicolons and execute each statement
        String[] statements = script.split(";");
        
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                stmt.executeUpdate(trimmed);
            }
        }
        
        stmt.close();
    }
    
    /**
     * Gets database metadata information
     * @return Database metadata as string
     */
    public String getDatabaseInfo() {
        try {
            DatabaseMetaData metaData = getConnection().getMetaData();
            StringBuilder info = new StringBuilder();
            
            info.append("Database Product: ").append(metaData.getDatabaseProductName()).append("\n");
            info.append("Database Version: ").append(metaData.getDatabaseProductVersion()).append("\n");
            info.append("Driver Name: ").append(metaData.getDriverName()).append("\n");
            info.append("Driver Version: ").append(metaData.getDriverVersion()).append("\n");
            info.append("URL: ").append(metaData.getURL()).append("\n");
            info.append("Username: ").append(metaData.getUserName()).append("\n");
            
            return info.toString();
            
        } catch (SQLException e) {
            return "Failed to get database info: " + e.getMessage();
        }
    }
    
    /**
     * Creates a default admin user if no users exist
     * @throws SQLException if database operation fails
     */
    public void createDefaultAdminUser() throws SQLException {
        // Check if any users exist
        String checkQuery = "SELECT COUNT(*) FROM users";
        PreparedStatement checkStmt = getConnection().prepareStatement(checkQuery);
        ResultSet rs = checkStmt.executeQuery();
        
        rs.next();
        int userCount = rs.getInt(1);
        rs.close();
        checkStmt.close();
        
        if (userCount == 0) {
            // Create default admin user
            String insertQuery = "INSERT INTO users (username, password_hash, email, full_name, role) " +
                "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement insertStmt = getConnection().prepareStatement(insertQuery);
            insertStmt.setString(1, "admin");
            insertStmt.setString(2, "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9"); // "admin123" SHA-256 hashed
            insertStmt.setString(3, "admin@taskmanager.com");
            insertStmt.setString(4, "System Administrator");
            insertStmt.setString(5, "ADMIN");
            
            insertStmt.executeUpdate();
            insertStmt.close();
            
            System.out.println("Default admin user created:");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("Please change the password after first login!");
        }
    }
    
    /**
     * Performs database cleanup and optimization
     */
    public void performMaintenance() {
        try {
            Statement stmt = getConnection().createStatement();
            
            // Optimize tables
            stmt.executeUpdate("OPTIMIZE TABLE users");
            stmt.executeUpdate("OPTIMIZE TABLE projects");
            stmt.executeUpdate("OPTIMIZE TABLE tasks");
            stmt.executeUpdate("OPTIMIZE TABLE status_history");
            
            // Clean up old status history (keep only last 1000 records per entity)
            String cleanupQuery = "DELETE sh1 FROM status_history sh1 " +
                "INNER JOIN (" +
                "SELECT entity_type, entity_id, " +
                "ROW_NUMBER() OVER (PARTITION BY entity_type, entity_id ORDER BY changed_at DESC) as rn " +
                "FROM status_history" +
                ") sh2 ON sh1.entity_type = sh2.entity_type " +
                "AND sh1.entity_id = sh2.entity_id " +
                "WHERE sh2.rn > 1000";
            
            stmt.executeUpdate(cleanupQuery);
            stmt.close();
            
            System.out.println("Database maintenance completed");
            
        } catch (SQLException e) {
            System.err.println("Database maintenance failed: " + e.getMessage());
        }
    }
}