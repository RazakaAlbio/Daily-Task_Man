package dao;

import models.Project;
import models.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProjectDAO class extending BaseDAO
 * Demonstrates inheritance and concrete implementation of abstract methods
 */
public class ProjectDAO extends BaseDAO<Project> {
    
    private UserDAO userDAO;
    
    /**
     * Constructor initializes UserDAO for user lookups
     */
    public ProjectDAO() {
        super();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return Table name for projects
     */
    @Override
    protected String getTableName() {
        return "projects";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Maps ResultSet to Project entity
     * @param rs ResultSet from database query
     * @return Project object
     * @throws SQLException if mapping fails
     */
    @Override
    protected Project mapResultSetToEntity(ResultSet rs) throws SQLException {
        // Get the user who created the project
        User createdBy = userDAO.findById(rs.getInt("created_by"));
        
        Project project = new Project(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            Project.Status.valueOf(rs.getString("status")),
            createdBy
        );
        
        // Set timestamps
        project.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        project.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        return project;
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return SQL insert statement for projects
     */
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO projects (name, description, status, creator_id) VALUES (?, ?, ?, ?)";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * @return SQL update statement for projects
     */
    @Override
    protected String getUpdateSQL() {
        return "UPDATE projects SET name = ?, description = ?, status = ? WHERE id = ?";
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Sets parameters for insert statement
     * @param stmt PreparedStatement
     * @param project Project entity to insert
     * @throws SQLException if parameter setting fails
     */
    @Override
    protected void setInsertParameters(PreparedStatement stmt, Project project) throws SQLException {
        stmt.setString(1, project.getName());
        stmt.setString(2, project.getDescription());
        stmt.setString(3, project.getStatus().name());
        stmt.setInt(4, project.getCreator().getId());
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Sets parameters for update statement
     * @param stmt PreparedStatement
     * @param project Project entity to update
     * @throws SQLException if parameter setting fails
     */
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, Project project) throws SQLException {
        stmt.setString(1, project.getName());
        stmt.setString(2, project.getDescription());
        stmt.setString(3, project.getStatus().name());
        stmt.setInt(4, project.getId());
    }
    
    /**
     * Implementation of abstract method from BaseDAO
     * Finds all projects
     * @return List of all projects
     */
    @Override
    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all projects: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Finds projects by status
     * @param status Project status to filter by
     * @return List of projects with specified status
     */
    public List<Project> findByStatus(Project.Status status) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE status = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding projects by status: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Finds projects created by a specific user
     * @param userId User ID who created the projects
     * @return List of projects created by the user
     */
    public List<Project> findByCreatedBy(int userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " WHERE created_by = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding projects by creator: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Finds project by name
     * @param name Project name to search for
     * @return Project object or null if not found
     */
    public Project findByName(String name) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding project by name: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Searches projects by name (partial match)
     * @param searchTerm Search term for project name
     * @return List of projects matching the search term
     */
    public List<Project> searchByName(String searchTerm) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE name LIKE ? ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching projects by name: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Gets active projects (projects that can have tasks added)
     * @return List of active projects
     */
    public List<Project> getActiveProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + 
                    " WHERE status IN ('ACTIVE', 'PAUSED') ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding active projects: " + e.getMessage());
        }
        
        return projects;
    }
    
    /**
     * Gets project statistics
     * @param projectId Project ID
     * @return Array containing [totalTasks, completedTasks, inProgressTasks, todoTasks]
     */
    public int[] getProjectStatistics(int projectId) {
        String sql = "SELECT " +
            "COUNT(*) as total_tasks," +
            "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_tasks," +
            "SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_tasks," +
            "SUM(CASE WHEN status = 'TODO' THEN 1 ELSE 0 END) as todo_tasks " +
            "FROM tasks " +
            "WHERE project_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new int[]{
                    rs.getInt("total_tasks"),
                    rs.getInt("completed_tasks"),
                    rs.getInt("in_progress_tasks"),
                    rs.getInt("todo_tasks")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error getting project statistics: " + e.getMessage());
        }
        
        return new int[]{0, 0, 0, 0};
    }
    
    /**
     * Checks if project name already exists
     * @param name Project name to check
     * @return true if name exists, false otherwise
     */
    public boolean projectNameExists(String name) {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking project name existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Updates project status
     * @param projectId Project ID
     * @param newStatus New status
     * @return true if update successful, false otherwise
     */
    public boolean updateStatus(int projectId, Project.Status newStatus) {
        String sql = "UPDATE " + getTableName() + " SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, projectId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating project status: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Gets projects with their task counts
     * @return List of projects with task statistics
     */
    public List<Object[]> getProjectsWithTaskCounts() {
        List<Object[]> results = new ArrayList<>();
        String sql = "SELECT p.*, " +
            "COUNT(t.id) as task_count," +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_count " +
            "FROM projects p " +
            "LEFT JOIN tasks t ON p.id = t.project_id " +
            "GROUP BY p.id, p.name, p.description, p.status, p.created_by, p.created_at, p.updated_at " +
            "ORDER BY p.created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Project project = mapResultSetToEntity(rs);
                int taskCount = rs.getInt("task_count");
                int completedCount = rs.getInt("completed_count");
                results.add(new Object[]{project, taskCount, completedCount});
            }
        } catch (SQLException e) {
            System.err.println("Error getting projects with task counts: " + e.getMessage());
        }
        
        return results;
    }
}