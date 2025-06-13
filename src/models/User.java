package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * User class extending BaseEntity
 * Demonstrates inheritance, encapsulation, and enum usage
 */
public class User extends BaseEntity {
    
    /**
     * Enum for user roles - demonstrates enum usage
     */
    public enum Role {
        ADMIN("Administrator", 3),
        MANAGER("Manager", 2),
        EMPLOYEE("Employee", 1);
        
        private final String displayName;
        private final int level;
        
        Role(String displayName, int level) {
            this.displayName = displayName;
            this.level = level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean canAssignTo(Role targetRole) {
            return this.level >= targetRole.level;
        }
    }
    
    // Private fields for encapsulation
    private String username;
    private String password; // Hashed password
    private String email;
    private Role role;
    private String fullName;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    /**
     * Default constructor
     */
    public User() {
        super();
        this.role = Role.EMPLOYEE; // Default role
    }
    
    /**
     * Constructor with parameters
     * @param username User's username
     * @param password User's password (will be hashed)
     * @param email User's email
     * @param role User's role
     * @param fullName User's full name
     */
    public User(String username, String password, String email, Role role, String fullName) {
        super();
        this.username = username;
        this.password = hashPassword(password);
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }
    
    /**
     * Constructor with ID (for database retrieval)
     */
    public User(int id, String username, String hashedPassword, String email, Role role, String fullName) {
        super(id);
        this.username = username;
        this.password = hashedPassword; // Already hashed from database
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }
    
    // Getters and Setters with proper encapsulation
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim();
            updateTimestamp();
        }
    }
    
    /**
     * Sets password with automatic hashing
     * @param password Plain text password
     */
    public void setPassword(String password) {
        if (password != null && password.length() >= 6) {
            this.password = hashPassword(password);
            updateTimestamp();
        }
    }
    
    /**
     * Verifies password against stored hash
     * @param password Plain text password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String password) {
        return this.password.equals(hashPassword(password));
    }
    
    /**
     * Gets hashed password (protected access for database operations)
     * @return Hashed password
     */
    protected String getHashedPassword() {
        return password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        if (isValidEmail(email)) {
            this.email = email;
            updateTimestamp();
        }
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        if (role != null) {
            this.role = role;
            updateTimestamp();
        }
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName.trim();
            updateTimestamp();
        }
    }
    
    /**
     * Implementation of abstract method from BaseEntity
     * @return Display name for the user
     */
    @Override
    public String getDisplayName() {
        return fullName != null ? fullName : username;
    }
    
    /**
     * Implementation of abstract method from BaseEntity
     * Validates user data
     * @return true if user data is valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.isEmpty() &&
               email != null && isValidEmail(email) &&
               role != null &&
               fullName != null && !fullName.trim().isEmpty();
    }
    
    /**
     * Checks if user can assign tasks to another user
     * @param targetUser User to assign task to
     * @return true if assignment is allowed, false otherwise
     */
    public boolean canAssignTo(User targetUser) {
        if (targetUser == null) return false;
        return this.role.canAssignTo(targetUser.role);
    }
    
    /**
     * Checks if user is admin
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
    
    /**
     * Checks if user is manager or above
     * @return true if user is manager or admin, false otherwise
     */
    public boolean isManagerOrAbove() {
        return role == Role.MANAGER || role == Role.ADMIN;
    }
    
    /**
     * Private method to hash passwords using SHA-256
     * @param password Plain text password
     * @return Hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Private method to validate email format
     * @param email Email to validate
     * @return true if email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Override toString for better representation
     * Demonstrates method overriding
     */
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', fullName='%s', role=%s, email='%s'}",
                           id, username, fullName, role.getDisplayName(), email);
    }
    
    /**
     * Override equals method for proper user comparison
     * Demonstrates method overriding
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }
    
    /**
     * Override hashCode method
     * Demonstrates method overriding
     */
    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}