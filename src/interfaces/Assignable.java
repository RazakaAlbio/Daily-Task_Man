package interfaces;

import models.User;

/**
 * Interface for entities that can be assigned to users
 * Demonstrates interface usage and contract definition
 */
public interface Assignable {
    
    /**
     * Assigns the entity to a user
     * @param user User to assign to
     * @param assignedBy User who is making the assignment
     * @return true if assignment successful, false otherwise
     */
    boolean assignTo(User user, User assignedBy);
    
    /**
     * Unassigns the entity from current user
     * @param unassignedBy User who is removing the assignment
     * @return true if unassignment successful, false otherwise
     */
    boolean unassign(User unassignedBy);
    
    /**
     * Gets the currently assigned user
     * @return User object or null if not assigned
     */
    User getAssignedUser();
    
    /**
     * Gets the user who made the assignment
     * @return User object who assigned this entity
     */
    User getAssignedBy();
    
    /**
     * Checks if the entity is currently assigned
     * @return true if assigned, false otherwise
     */
    boolean isAssigned();
}