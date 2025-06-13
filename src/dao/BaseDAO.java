package dao;

import config.DatabaseConfig;
import models.BaseEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Abstract base DAO class providing common database operations
 * Demonstrates abstract class usage and template method pattern
 * @param <T> Entity type extending BaseEntity
 */
public abstract class BaseDAO<T extends BaseEntity> {
    
    protected Connection connection;
    
    /**
     * Constructor initializes database connection
     */
    public BaseDAO() {
        try {
            this.connection = DatabaseConfig.getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection: " + e.getMessage());
        }
    }
    
    /**
     * Abstract method to get table name
     * Must be implemented by subclasses
     * @return Table name
     */
    protected abstract String getTableName();
    
    /**
     * Abstract method to map ResultSet to entity
     * Must be implemented by subclasses
     * @param rs ResultSet from database query
     * @return Entity object
     * @throws SQLException if mapping fails
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    
    /**
     * Abstract method to get insert SQL statement
     * Must be implemented by subclasses
     * @return SQL insert statement
     */
    protected abstract String getInsertSQL();
    
    /**
     * Abstract method to get update SQL statement
     * Must be implemented by subclasses
     * @return SQL update statement
     */
    protected abstract String getUpdateSQL();
    
    /**
     * Abstract method to set parameters for insert statement
     * Must be implemented by subclasses
     * @param stmt PreparedStatement
     * @param entity Entity to insert
     * @throws SQLException if parameter setting fails
     */
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    /**
     * Abstract method to set parameters for update statement
     * Must be implemented by subclasses
     * @param stmt PreparedStatement
     * @param entity Entity to update
     * @throws SQLException if parameter setting fails
     */
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    /**
     * Template method for finding entity by ID
     * Demonstrates template method pattern
     * @param id Entity ID
     * @return Entity object or null if not found
     */
    public T findById(int id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding entity by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Template method for finding all entities
     * Demonstrates template method pattern
     * @return List of all entities
     */
    public abstract List<T> findAll();
    
    /**
     * Template method for saving entity (insert or update)
     * Demonstrates template method pattern
     * @param entity Entity to save
     * @return true if save successful, false otherwise
     */
    public boolean save(T entity) {
        if (entity == null || !entity.isValid()) {
            return false;
        }
        
        if (entity.getId() == 0) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }
    
    /**
     * Template method for inserting new entity
     * @param entity Entity to insert
     * @return true if insert successful, false otherwise
     */
    protected boolean insert(T entity) {
        String sql = getInsertSQL();
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, entity);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting entity: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Template method for updating existing entity
     * @param entity Entity to update
     * @return true if update successful, false otherwise
     */
    protected boolean update(T entity) {
        String sql = getUpdateSQL();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setUpdateParameters(stmt, entity);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating entity: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Template method for deleting entity by ID
     * @param id Entity ID to delete
     * @return true if delete successful, false otherwise
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting entity: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Template method for counting entities
     * @return Number of entities in table
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + getTableName();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting entities: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Utility method to close resources safely
     * @param rs ResultSet to close
     * @param stmt PreparedStatement to close
     */
    protected void closeResources(ResultSet rs, PreparedStatement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
    
    /**
     * Utility method to execute custom query
     * @param sql SQL query
     * @param parameters Query parameters
     * @return ResultSet
     * @throws SQLException if query execution fails
     */
    protected ResultSet executeQuery(String sql, Object... parameters) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            stmt.setObject(i + 1, parameters[i]);
        }
        return stmt.executeQuery();
    }
    
    /**
     * Utility method to execute custom update
     * @param sql SQL update statement
     * @param parameters Update parameters
     * @return Number of affected rows
     * @throws SQLException if update execution fails
     */
    protected int executeUpdate(String sql, Object... parameters) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            return stmt.executeUpdate();
        }
    }
}