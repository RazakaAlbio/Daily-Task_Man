package interfaces;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for entities that can track status changes
 * Demonstrates interface usage for audit trail functionality
 */
public interface Trackable {
    
    /**
     * Gets the current status of the entity
     * @return Current status as string
     */
    String getCurrentStatus();
    
    /**
     * Updates the status of the entity
     * @param newStatus New status to set
     * @param updatedBy User making the status change
     * @return true if status update successful, false otherwise
     */
    boolean updateStatus(String newStatus, String updatedBy);
    
    /**
     * Gets the history of status changes
     * @return List of status change records
     */
    List<String> getStatusHistory();
    
    /**
     * Gets the last update timestamp
     * @return LocalDateTime of last update
     */
    LocalDateTime getLastUpdated();
    
    /**
     * Checks if the entity can be updated
     * @return true if updatable, false otherwise
     */
    boolean isUpdatable();
}