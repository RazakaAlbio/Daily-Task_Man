package dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Project;
import models.Task;
import models.User;

/**
 * TaskDAO class extending BaseDAO
 * Demonstrates inheritance and concrete implementation of abstract methods
 */
public class TaskDAO extends BaseDAO<Task> {
    
    private UserDAO userDAO;
    private ProjectDAO projectDAO;
    
    /**
     * Constructor initializes related DAOs
     */
    public TaskDAO() {
        super();
        this.userDAO = new UserDAO();
        this.projectDAO = new ProjectDAO();
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return Table name for tasks
     */
    @Override
    protected String getTableName() {
        return "tasks";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Maps ResultSet to Task entity
     * @param rs ResultSet from database query
     * @return Task object
     * @throws SQLException if mapping fails
     */
    @Override
    protected Task mapResultSetToEntity(ResultSet rs) throws SQLException {
        // Get related entities
        Project project = null;
        if (rs.getInt("project_id") != 0) {
            project = projectDAO.findById(rs.getInt("project_id"));
        }
        
        User assignedTo = null;
        if (rs.getInt("assigned_user_id") != 0) {
            assignedTo = userDAO.findById(rs.getInt("assigned_user_id"));
        }
        
        User assignedBy = null;
        if (rs.getInt("assigner_id") != 0) {
            assignedBy = userDAO.findById(rs.getInt("assigner_id"));
        }
        
        Task task = new Task(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            Task.Status.valueOf(rs.getString("status")),
            Task.Priority.valueOf(rs.getString("priority")),
            project,
            assignedTo,
            assignedBy,
            rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null
        );
        
        // Set timestamps
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return task;
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return SQL insert statement for tasks
     */
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO tasks (title, description, status, priority, project_id, assigned_user_id, assigner_id, due_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return SQL update statement for tasks
     */
    @Override
    protected String getUpdateSQL() {
        return "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, project_id = ?, assigned_user_id = ?, assigner_id = ?, due_date = ? WHERE id = ?";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Sets parameters for insert statement
     * @param stmt PreparedStatement
     * @param task Task entity to insert
     * @throws SQLException if parameter setting fails
     */
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Task task) throws SQLException {
        stmt.setString(1, task.getTitle());
        stmt.setString(2, task.getDescription());
        stmt.setString(3, task.getStatus().name());
        stmt.setString(4, task.getPriority().name());
        stmt.setObject(5, task.getProject() != null ? task.getProject().getId() : null);
        stmt.setObject(6, task.getAssignedUser() != null ? task.getAssignedUser().getId() : null);
        stmt.setObject(7, task.getAssignedBy() != null ? task.getAssignedBy().getId() : null);
        stmt.setDate(8, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Sets parameters for update statement
     * @param stmt PreparedStatement
     * @param task Task entity to update
     * @throws SQLException if parameter setting fails
     */
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Task task) throws SQLException {
        stmt.setString(1, task.getTitle());
        stmt.setString(2, task.getDescription());
        stmt.setString(3, task.getStatus().name());
        stmt.setString(4, task.getPriority().name());
        stmt.setObject(5, task.getProject() != null ? task.getProject().getId() : null);
        stmt.setObject(6, task.getAssignedUser() != null ? task.getAssignedUser().getId() : null);
        stmt.setObject(7, task.getAssignedBy() != null ? task.getAssignedBy().getId() : null);
        stmt.setDate(8, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
        stmt.setInt(9, task.getId());
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Finds all tasks
     * @return List of all tasks
     */
    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all tasks: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Finds tasks by status
     * @param status Task status to filter by
     * @return List of tasks with specified status
     */
    public List<Task> findByStatus(Task.Status status) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE status = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding tasks by status: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Finds tasks assigned to a specific user
     * @param userId User ID
     * @return List of tasks assigned to the user
     */
    public List<Task> findByAssignedUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE assigned_user_id = ? ORDER BY due_date ASC, priority DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding tasks by assigned user: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Finds tasks in a specific project
     * @param projectId Project ID
     * @return List of tasks in the project
     */
    public List<Task> findByProject(int projectId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE project_id = ? ORDER BY priority DESC, due_date ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding tasks by project: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Finds tasks by priority
     * @param priority Task priority to filter by
     * @return List of tasks with specified priority
     */
    public List<Task> findByPriority(Task.Priority priority) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE priority = ? ORDER BY due_date ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, priority.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding tasks by priority: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Finds overdue tasks
     * @return List of overdue tasks
     */
    public List<Task> findOverdueTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE due_date < CURDATE() AND status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY due_date ASC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding overdue tasks: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Finds tasks due today
     * @return List of tasks due today
     */
    public List<Task> findTasksDueToday() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE due_date = CURDATE() AND status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY priority DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding tasks due today: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Searches tasks by title (partial match)
     * @param searchTerm Search term for task title
     * @return List of tasks matching the search term
     */
    public List<Task> searchByTitle(String searchTerm) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE title LIKE ? ORDER BY title";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching tasks by title: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Gets unassigned tasks
     * @return List of unassigned tasks
     */
    public List<Task> getUnassignedTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE assigned_user_id IS NULL ORDER BY priority DESC, created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tasks.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding unassigned tasks: " + e.getMessage());
        }
        
        return tasks;
    }
    
    /**
     * Updates task status
     * @param taskId Task ID
     * @param newStatus New status
     * @return true if update successful, false otherwise
     */
    public boolean updateStatus(int taskId, Task.Status newStatus) {
        String sql = "UPDATE " + getTableName() + " SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, taskId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task status: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Assigns task to user
     * @param taskId Task ID
     * @param assignedToId User ID to assign to
     * @param assignedById User ID who is assigning
     * @return true if assignment successful, false otherwise
     */
    public boolean assignTask(int taskId, int assignedToId, int assignedById) {
        String sql = "UPDATE " + getTableName() + 
                    " SET assigned_user_id = ?, assigner_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, assignedToId);
            stmt.setInt(2, assignedById);
            stmt.setInt(3, taskId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error assigning task: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Unassigns task from user
     * @param taskId Task ID
     * @return true if unassignment successful, false otherwise
     */
    public boolean unassignTask(int taskId) {
        String sql = "UPDATE " + getTableName() + 
                    " SET assigned_user_id = NULL, assigner_id = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error unassigning task: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Gets task statistics for dashboard
     * @return Array containing [totalTasks, todoTasks, inProgressTasks, completedTasks, overdueTasks]
     */
    public int[] getTaskStatistics() {
        String sql = "SELECT " +
            "COUNT(*) as total_tasks," +
            "SUM(CASE WHEN status = 'TODO' THEN 1 ELSE 0 END) as todo_tasks," +
            "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_tasks," +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks," +
            "SUM(CASE WHEN due_date < CURDATE() AND status NOT IN ('COMPLETED', 'CANCELLED') THEN 1 ELSE 0 END) as overdue_tasks " +
            "FROM tasks";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new int[]{
                    rs.getInt("total_tasks"),
                    rs.getInt("todo_tasks"),
                    rs.getInt("in_progress_tasks"),
                    rs.getInt("completed_tasks"),
                    rs.getInt("overdue_tasks")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error getting task statistics: " + e.getMessage());
        }
        
        return new int[]{0, 0, 0, 0, 0};
    }
    
    /**
     * Gets user task statistics
     * @param userId User ID
     * @return Array containing [assignedTasks, completedTasks, overdueTasks]
     */
    public int[] getUserTaskStatistics(int userId) {
        String sql = "SELECT " +
            "COUNT(*) as assigned_tasks," +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks," +
            "SUM(CASE WHEN due_date < CURDATE() AND status NOT IN ('COMPLETED', 'CANCELLED') THEN 1 ELSE 0 END) as overdue_tasks " +
            "FROM tasks " +
            "WHERE assigned_user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new int[]{
                    rs.getInt("assigned_tasks"),
                    rs.getInt("completed_tasks"),
                    rs.getInt("overdue_tasks")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error getting user task statistics: " + e.getMessage());
        }
        
        return new int[]{0, 0, 0};
    }
}