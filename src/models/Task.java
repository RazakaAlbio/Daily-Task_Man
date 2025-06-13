package models;

import interfaces.Assignable;
import interfaces.Trackable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Task class extending BaseEntity and implementing multiple interfaces
 * Demonstrates inheritance, multiple interface implementation, and polymorphism
 */
public class Task extends BaseEntity implements Assignable, Trackable {
    
    /**
     * Enum for task status
     */
    public enum Status {
        TODO("To Do", false),
        IN_PROGRESS("In Progress", false),
        COMPLETED("Completed", true),
        CANCELLED("Cancelled", true);
        
        private final String displayName;
        private final boolean isFinal;
        
        Status(String displayName, boolean isFinal) {
            this.displayName = displayName;
            this.isFinal = isFinal;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean isFinal() {
            return isFinal;
        }
    }
    
    /**
     * Enum for task priority
     */
    public enum Priority {
        LOW("Low", 1, "#28a745"),
        MEDIUM("Medium", 2, "#ffc107"),
        HIGH("High", 3, "#fd7e14"),
        URGENT("Urgent", 4, "#dc3545");
        
        private final String displayName;
        private final int level;
        private final String color;
        
        Priority(String displayName, int level, String color) {
            this.displayName = displayName;
            this.level = level;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    // Private fields for encapsulation
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Project project; // HAS-A relationship with Project
    private User assignedTo; // HAS-A relationship with User (assigned user)
    private User assignedBy; // HAS-A relationship with User (assigner)
    private LocalDate dueDate;
    private List<String> statusHistory; // For tracking status changes
    
    /**
     * Default constructor
     */
    public Task() {
        super();
        this.status = Status.TODO;
        this.priority = Priority.MEDIUM;
        this.statusHistory = new ArrayList<>();
        this.statusHistory.add("Task created with status: " + status.getDisplayName());
    }
    
    /**
     * Constructor with parameters
     * @param title Task title
     * @param description Task description
     * @param priority Task priority
     * @param dueDate Task due date
     */
    public Task(String title, String description, Priority priority, LocalDate dueDate) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
    }
    
    /**
     * Constructor with ID (for database retrieval)
     */
    public Task(int id, String title, String description, Status status, Priority priority, 
                Project project, User assignedTo, User assignedBy, LocalDate dueDate) {
        super(id);
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.project = project;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.dueDate = dueDate;
        this.statusHistory = new ArrayList<>();
    }
    
    // Getters and Setters with proper encapsulation
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
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
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        if (priority != null) {
            this.priority = priority;
            updateTimestamp();
        }
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        updateTimestamp();
    }
    
    /**
     * Checks if task is overdue
     * @return true if task is overdue, false otherwise
     */
    public boolean isOverdue() {
        return dueDate != null && 
               LocalDate.now().isAfter(dueDate) && 
               status != Status.COMPLETED && 
               status != Status.CANCELLED;
    }
    
    /**
     * Gets days until due date
     * @return Number of days (negative if overdue)
     */
    public long getDaysUntilDue() {
        if (dueDate == null) return Long.MAX_VALUE;
        return LocalDate.now().until(dueDate).getDays();
    }
    
    /**
     * Implementation of abstract method from BaseEntity
     * @return Display name for the task
     */
    @Override
    public String getDisplayName() {
        return title;
    }
    
    /**
     * Implementation of abstract method from BaseEntity
     * Validates task data
     * @return true if task data is valid, false otherwise
     */
    @Override
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               status != null &&
               priority != null;
    }
    
    // Implementation of Assignable interface methods
    
    /**
     * Assigns the task to a user
     * @param user User to assign to
     * @param assignedBy User who is making the assignment
     * @return true if assignment successful, false otherwise
     */
    @Override
    public boolean assignTo(User user, User assignedBy) {
        if (user == null || assignedBy == null) return false;
        if (!assignedBy.canAssignTo(user)) return false;
        if (status.isFinal()) return false;
        
        this.assignedTo = user;
        this.assignedBy = assignedBy;
        updateTimestamp();
        statusHistory.add(String.format("Task assigned to %s by %s at %s",
                user.getDisplayName(), assignedBy.getDisplayName(), LocalDateTime.now()));
        return true;
    }
    
    /**
     * Unassigns the task from current user
     * @param unassignedBy User who is removing the assignment
     * @return true if unassignment successful, false otherwise
     */
    @Override
    public boolean unassign(User unassignedBy) {
        if (unassignedBy == null || assignedTo == null) return false;
        if (!unassignedBy.isManagerOrAbove() && !unassignedBy.equals(assignedTo)) return false;
        if (status.isFinal()) return false;
        
        String previousAssignee = assignedTo.getDisplayName();
        this.assignedTo = null;
        this.assignedBy = null;
        updateTimestamp();
        statusHistory.add(String.format("Task unassigned from %s by %s at %s",
                previousAssignee, unassignedBy.getDisplayName(), LocalDateTime.now()));
        return true;
    }
    
    /**
     * Gets the currently assigned user
     * @return User object or null if not assigned
     */
    @Override
    public User getAssignedUser() {
        return assignedTo;
    }
    
    /**
     * Gets the user who made the assignment
     * @return User object who assigned this task
     */
    @Override
    public User getAssignedBy() {
        return assignedBy;
    }
    
    /**
     * Checks if the task is currently assigned
     * @return true if assigned, false otherwise
     */
    @Override
    public boolean isAssigned() {
        return assignedTo != null;
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
     * Updates the task status
     * @param newStatus New status to set
     * @param updatedBy User making the status change
     * @return true if status update successful, false otherwise
     */
    @Override
    public boolean updateStatus(String newStatus, String updatedBy) {
        try {
            Status status = Status.valueOf(newStatus.toUpperCase().replace(" ", "_"));
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
     * Checks if the task can be updated
     * @return true if updatable, false otherwise
     */
    @Override
    public boolean isUpdatable() {
        return !status.isFinal();
    }
    
    /**
     * Override toString for better representation
     * Demonstrates method overriding and polymorphism
     */
    @Override
    public String toString() {
        String assigneeInfo = assignedTo != null ? assignedTo.getDisplayName() : "Unassigned";
        String projectInfo = project != null ? project.getName() : "No Project";
        return String.format("Task{id=%d, title='%s', status=%s, priority=%s, assignee=%s, project=%s}",
                           id, title, status.getDisplayName(), priority.getDisplayName(), assigneeInfo, projectInfo);
    }
    
    /**
     * Override equals method for proper task comparison
     * Demonstrates method overriding
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Task task = (Task) obj;
        return title.equals(task.title) && 
               (project != null ? project.equals(task.project) : task.project == null);
    }
    
    /**
     * Override hashCode method
     * Demonstrates method overriding
     */
    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (project != null ? project.hashCode() : 0);
        return result;
    }
}