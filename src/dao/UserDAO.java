package dao;

import models.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO class extending BaseDAO
 * Demonstrates inheritance and concrete implementation of abstract methods
 */
public class UserDAO extends BaseDAO<User> {
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return Table name for users
     */
    @Override
    protected String getTableName() {
        return "users";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Maps ResultSet to User entity
     * @param rs ResultSet from database query
     * @return User object
     * @throws SQLException if mapping fails
     */
    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"), // Already hashed
            rs.getString("email"),
            User.Role.valueOf(rs.getString("role")),
            rs.getString("full_name")
        );
        
        // Set timestamps
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return user;
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return SQL insert statement for users
     */
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO users (username, password_hash, email, role, full_name) VALUES (?, ?, ?, ?, ?)";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return SQL update statement for users
     */
    @Override
    protected String getUpdateSQL() {
        return "UPDATE users SET username = ?, password_hash = ?, email = ?, role = ?, full_name = ? WHERE id = ?";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Sets parameters for insert statement
     * @param stmt PreparedStatement
     * @param user User entity to insert
     * @throws SQLException if parameter setting fails
     */
    @Override
    protected void setInsertParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getHashedPassword());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getRole().name());
        stmt.setString(5, user.getFullName());
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Sets parameters for update statement
     * @param stmt PreparedStatement
     * @param user User entity to update
     * @throws SQLException if parameter setting fails
     */
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getHashedPassword());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getRole().name());
        stmt.setString(5, user.getFullName());
        stmt.setInt(6, user.getId());
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Finds all users
     * @return List of all users
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY full_name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Finds user by username
     * Demonstrates method overloading
     * @param username Username to search for
     * @return User object or null if not found
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Finds user by email
     * Demonstrates method overloading
     * @param email Email to search for
     * @return User object or null if not found
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Authenticates user with username and password
     * @param username Username
     * @param password Plain text password
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.verifyPassword(password)) {
            return user;
        }
        return null;
    }
    
    /**
     * Finds users by role
     * @param role User role to filter by
     * @return List of users with specified role
     */
    public List<User> findByRole(User.Role role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE role = ? ORDER BY full_name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding users by role: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Checks if username already exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Checks if email already exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Updates user password
     * @param userId User ID
     * @param newPassword New password (will be hashed)
     * @return true if update successful, false otherwise
     */
    public boolean updatePassword(int userId, String newPassword) {
        User user = findById(userId);
        if (user != null) {
            user.setPassword(newPassword); // This will hash the password
            return update(user);
        }
        return false;
    }
    
    /**
     * Gets users who can be assigned tasks (employees and managers)
     * @return List of assignable users
     */
    public List<User> getAssignableUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE role IN ('EMPLOYEE', 'MANAGER') ORDER BY full_name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding assignable users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Gets users who can assign tasks (managers and admins)
     * @return List of users who can assign tasks
     */
    public List<User> getTaskAssigners() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE role IN ('MANAGER', 'ADMIN') ORDER BY full_name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding task assigners: " + e.getMessage());
        }
        
        return users;
    }
}