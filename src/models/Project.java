package models;

import interfaces.Trackable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project class extending BaseEntity and implementing Trackable interface
 * Demonstrates inheritance, interface implementation, and composition (HAS-A relationship)
 */
public class Project extends BaseEntity implements Trackable {
    
    /**
     * Enum for project status
     */
    public enum Status {
        ACTIVE("Active", true),
        COMPLETED("Completed", false),
        PAUSED("Paused", true);
        
        private final String displayName;
        private final boolean canAddTasks;
        
        Status(String displayName, boolean canAddTasks) {
            this.displayName = displayName;
            this.canAddTasks = canAddTasks;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean canAddTasks() {
            return canAddTasks;
        }
    }
    
    // Private fields for encapsulation
    private String name;
    private String description;
    private Status status;
    private User createdBy; // HAS-A relationship with User
    private List<Task> tasks; // HAS-A relationship with Task (composition)
    private List<String> statusHistory; // For tracking status changes
    
    /**
     * Default constructor
     */
    public Project() {
        super();
        this.status = Status.ACTIVE;
        this.tasks = new ArrayList<>();
        this.statusHistory = new ArrayList<>();
        this.statusHistory.add("Project created with status: " + status.getDisplayName());
    }
    
    /**
     * Constructor with parameters
     * @param name Project name
     * @param description Project description
     * @param createdBy User who created the project
     */
    public Project(String name, String description, User createdBy) {
        this();
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }
    
    /**
     * Constructor with ID (for database retrieval)
     */
    public Project(int id, String name, String description, Status status, User createdBy) {
        super(id);
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.tasks = new ArrayList<>();
        this.statusHistory = new ArrayList<>();
    }
    
    // Getters and Setters with proper encapsulation
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
            updateTimestamp();
        }
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        updateTimestamp();
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        if (status != null && this.status != status) {
            Status oldStatus = this.status;
            this.status = status;
            updateTimestamp();
            statusHistory.add(String.format("Status changed from %s to %s at %s",
                    oldStatus.getDisplayName(), status.getDisplayName(), LocalDateTime.now()));
        }
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    /**
     * Gets a copy of tasks list to maintain encapsulation
     * @return List of tasks
     */
    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }
    
    /**
     * Adds a task to the project
     * @param task Task to add
     * @return true if task added successfully, false otherwise
     */
    public boolean addTask(Task task) {
        if (task != null && status.canAddTasks() && !tasks.contains(task)) {
            tasks.add(task);
            task.setProject(this); // Set bidirectional relationship
            updateTimestamp();
            return true;
        }
        return false;
    }
    
    /**
     * Removes a task from the project
     * @param task Task to remove
     * @return true if task removed successfully, false otherwise
     */
    public boolean removeTask(Task task) {
        if (task != null && tasks.contains(task)) {
            tasks.remove(task);
            task.setProject(null); // Remove bidirectional relationship
            updateTimestamp();
            return true;
        }
        return false;
    }
    
    /**
     * Gets the number of tasks in the project
     * @return Number of tasks
     */
    public int getTaskCount() {
        return tasks.size();
    }
    
    /**
     * Gets completed tasks count
     * @return Number of completed tasks
     */
    public int getCompletedTasksCount() {
        return (int) tasks.stream()
                .filter(task -> task.getStatus() == Task.Status.COMPLETED)
                .count();
    }
    
    /**
     * Calculates project completion percentage
     * @return Completion percentage (0-100)
     */
    public double getCompletionPercentage() {
        if (tasks.isEmpty()) return 0.0;
        return (double) getCompletedTasksCount() / tasks.size() * 100.0;
    }
    
    /**
     * Implementation of abstract method from BaseEntity
     * @return Display name for the project
     */
    @Override
    public String getDisplayName() {
        return name;
    }
    
    /**
     * Implementation of abstract method from BaseEntity
     * Validates project data
     * @return true if project data is valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               status != null &&
               createdBy != null;
    }
    
    // Implementation of Trackable interface methods
    
    /**
     * Gets the current status as string
     * @return Current status display name
     */
    @Override
    public String getCurrentStatus() {
        return status.getDisplayName();
    }
    
    /**
     * Updates the project status
     * @param newStatus New status to set
     * @param updatedBy User making the status change
     * @return true if status update successful, false otherwise
     */
    @Override
    public boolean updateStatus(String newStatus, String updatedBy) {
        try {
            Status status = Status.valueOf(newStatus.toUpperCase());
            Status oldStatus = this.status;
            this.status = status;
            updateTimestamp();
            statusHistory.add(String.format("Status changed from %s to %s by %s at %s",
                    oldStatus.getDisplayName(), status.getDisplayName(), updatedBy, LocalDateTime.now()));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Gets the history of status changes
     * @return List of status change records
     */
    @Override
    public List<String> getStatusHistory() {
        return new ArrayList<>(statusHistory);
    }
    
    /**
     * Gets the last update timestamp
     * @return LocalDateTime of last update
     */
    @Override
    public LocalDateTime getLastUpdated() {
        return getUpdatedAt();
    }
    
    /**
     * Checks if the project can be updated
     * @return true if updatable, false otherwise
     */
    @Override
    public boolean isUpdatable() {
        return status != Status.COMPLETED;
    }
    
    /**
     * Override toString for better representation
     * Demonstrates method overriding
     */
    @Override
    public String toString() {
        return String.format("Project{id=%d, name='%s', status=%s, tasks=%d, completion=%.1f%%}",
                           id, name, status.getDisplayName(), getTaskCount(), getCompletionPercentage());
    }
    
    /**
     * Override equals method for proper project comparison
     * Demonstrates method overriding
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Project project = (Project) obj;
        return name.equals(project.name);
    }
    
    /**
     * Override hashCode method
     * Demonstrates method overriding
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}