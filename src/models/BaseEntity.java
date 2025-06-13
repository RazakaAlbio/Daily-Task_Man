package models;

import java.time.LocalDateTime;

/**
 * Abstract base class for all entities in the system
 * Demonstrates abstract class usage and provides common functionality
 */
public abstract class BaseEntity {
    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with ID
     * @param id Entity ID
     */
    public BaseEntity(int id) {
        this();
        this.id = id;
    }
    
    // Getters and Setters with proper encapsulation
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Abstract method to be implemented by subclasses
     * Demonstrates abstract method usage
     * @return String representation of the entity
     */
    public abstract String getDisplayName();
    
    /**
     * Abstract method for validation
     * Each entity must implement its own validation logic
     * @return true if entity is valid, false otherwise
     */
    public abstract boolean isValid();
    
    /**
     * Updates the updatedAt timestamp
     * Common functionality for all entities
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Override toString method
     * Provides polymorphic behavior
     */
    @Override
    public String toString() {
        return String.format("%s [ID: %d, Created: %s]", 
                           getDisplayName(), id, createdAt);
    }
    
    /**
     * Override equals method for proper object comparison
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity that = (BaseEntity) obj;
        return id == that.id;
    }
    
    /**
     * Override hashCode method
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}