-- Task Manager Database Initialization Script
-- MySQL Database Schema

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS task_manager 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE task_manager;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS status_history;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_active (is_active),
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create projects table
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status ENUM('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PLANNING',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    start_date DATE,
    end_date DATE,
    creator_id BIGINT NOT NULL,
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    budget DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_creator (creator_id),
    INDEX idx_dates (start_date, end_date),
    
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create tasks table
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status ENUM('TODO', 'IN_PROGRESS', 'REVIEW', 'DONE', 'CANCELLED') NOT NULL DEFAULT 'TODO',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    project_id BIGINT,
    assigned_user_id BIGINT,
    assigner_id BIGINT,
    estimated_hours DECIMAL(8,2),
    actual_hours DECIMAL(8,2),
    progress_percentage DECIMAL(5,2) DEFAULT 0.00,
    due_date DATETIME,
    completed_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    INDEX idx_title (title),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_project (project_id),
    INDEX idx_assigned_user (assigned_user_id),
    INDEX idx_assigner (assigner_id),
    INDEX idx_due_date (due_date),
    INDEX idx_created_at (created_at),
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigner_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create status_history table for audit trail
CREATE TABLE status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type ENUM('PROJECT', 'TASK') NOT NULL,
    entity_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_changed_by (changed_by),
    INDEX idx_changed_at (changed_at),
    
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- Create views for common queries

-- View for task details with related information
CREATE VIEW task_details AS
SELECT 
    t.id,
    t.title,
    t.description,
    t.status,
    t.priority,
    t.estimated_hours,
    t.actual_hours,
    t.progress_percentage,
    t.due_date,
    t.completed_at,
    t.created_at,
    t.updated_at,
    p.name AS project_name,
    p.status AS project_status,
    au.username AS assigned_username,
    au.full_name AS assigned_full_name,
    ar.username AS assigner_username,
    ar.full_name AS assigner_full_name,
    cu.username AS created_by_username,
    uu.username AS updated_by_username
FROM tasks t
LEFT JOIN projects p ON t.project_id = p.id
LEFT JOIN users au ON t.assigned_user_id = au.id
LEFT JOIN users ar ON t.assigner_id = ar.id
LEFT JOIN users cu ON t.created_by = cu.id
LEFT JOIN users uu ON t.updated_by = uu.id;

-- View for project statistics
CREATE VIEW project_stats AS
SELECT 
    p.id,
    p.name,
    p.status,
    p.priority,
    p.progress_percentage,
    p.created_at,
    COUNT(t.id) AS total_tasks,
    COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) AS completed_tasks,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) AS in_progress_tasks,
    COUNT(CASE WHEN t.status = 'TODO' THEN 1 END) AS pending_tasks,
    COUNT(CASE WHEN t.due_date < NOW() AND t.status != 'DONE' THEN 1 END) AS overdue_tasks,
    AVG(t.progress_percentage) AS avg_task_progress,
    u.username AS creator_username,
    u.full_name AS creator_full_name
FROM projects p
LEFT JOIN tasks t ON p.id = t.project_id
LEFT JOIN users u ON p.creator_id = u.id
GROUP BY p.id, p.name, p.status, p.priority, p.progress_percentage, p.created_at, u.username, u.full_name;

-- View for user task statistics
CREATE VIEW user_task_stats AS
SELECT 
    u.id,
    u.username,
    u.full_name,
    u.role,
    COUNT(t.id) AS total_assigned_tasks,
    COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) AS completed_tasks,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) AS in_progress_tasks,
    COUNT(CASE WHEN t.status = 'TODO' THEN 1 END) AS pending_tasks,
    COUNT(CASE WHEN t.due_date < NOW() AND t.status != 'DONE' THEN 1 END) AS overdue_tasks,
    AVG(t.progress_percentage) AS avg_progress,
    SUM(t.actual_hours) AS total_hours_worked
FROM users u
LEFT JOIN tasks t ON u.id = t.assigned_user_id
WHERE u.is_active = TRUE
GROUP BY u.id, u.username, u.full_name, u.role;

-- Insert sample data

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password_hash, email, full_name, role, created_by, updated_by) VALUES
('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPj.3xKqKzJGu', 'admin@company.com', 'System Administrator', 'ADMIN', 1, 1);

-- Insert sample users
INSERT INTO users (username, password_hash, email, full_name, role, created_by, updated_by) VALUES
('manager1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPj.3xKqKzJGu', 'manager1@company.com', 'John Manager', 'MANAGER', 1, 1),
('manager2', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPj.3xKqKzJGu', 'manager2@company.com', 'Sarah Manager', 'MANAGER', 1, 1),
('employee1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPj.3xKqKzJGu', 'employee1@company.com', 'Alice Employee', 'EMPLOYEE', 1, 1),
('employee2', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPj.3xKqKzJGu', 'employee2@company.com', 'Bob Employee', 'EMPLOYEE', 1, 1),
('employee3', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewfBPj.3xKqKzJGu', 'employee3@company.com', 'Charlie Employee', 'EMPLOYEE', 1, 1);

-- Insert sample projects
INSERT INTO projects (name, description, status, priority, start_date, end_date, creator_id, progress_percentage, budget, created_by, updated_by) VALUES
('Website Redesign', 'Complete redesign of company website with modern UI/UX', 'ACTIVE', 'HIGH', '2024-01-15', '2024-03-15', 2, 65.00, 50000.00, 1, 1),
('Mobile App Development', 'Develop mobile application for iOS and Android', 'PLANNING', 'URGENT', '2024-02-01', '2024-06-01', 2, 15.00, 80000.00, 1, 1),
('Database Migration', 'Migrate legacy database to new cloud infrastructure', 'ACTIVE', 'MEDIUM', '2024-01-01', '2024-02-28', 3, 80.00, 30000.00, 1, 1),
('Security Audit', 'Comprehensive security audit of all systems', 'COMPLETED', 'HIGH', '2023-12-01', '2024-01-31', 2, 100.00, 25000.00, 1, 1),
('Training Program', 'Employee training program for new technologies', 'ON_HOLD', 'LOW', '2024-03-01', '2024-05-01', 3, 25.00, 15000.00, 1, 1);

-- Insert sample tasks
INSERT INTO tasks (title, description, status, priority, project_id, assigned_user_id, assigner_id, estimated_hours, actual_hours, progress_percentage, due_date, created_by, updated_by) VALUES
('Design Homepage Mockup', 'Create wireframes and mockups for new homepage design', 'DONE', 'HIGH', 1, 4, 2, 16.00, 14.50, 100.00, '2024-01-25 17:00:00', 1, 1),
('Implement Responsive Layout', 'Code responsive CSS for mobile and tablet devices', 'IN_PROGRESS', 'HIGH', 1, 5, 2, 24.00, 18.00, 75.00, '2024-02-05 17:00:00', 1, 1),
('Setup Development Environment', 'Configure development tools and frameworks', 'DONE', 'MEDIUM', 1, 6, 2, 8.00, 6.00, 100.00, '2024-01-20 17:00:00', 1, 1),
('User Authentication Module', 'Implement login/logout and user management', 'IN_PROGRESS', 'HIGH', 1, 4, 2, 32.00, 20.00, 60.00, '2024-02-10 17:00:00', 1, 1),
('API Integration', 'Integrate with third-party APIs for data synchronization', 'TODO', 'MEDIUM', 1, 5, 2, 20.00, 0.00, 0.00, '2024-02-15 17:00:00', 1, 1),

('iOS App Architecture', 'Design and implement iOS app architecture', 'TODO', 'URGENT', 2, 4, 2, 40.00, 0.00, 0.00, '2024-02-20 17:00:00', 1, 1),
('Android App Architecture', 'Design and implement Android app architecture', 'TODO', 'URGENT', 2, 5, 2, 40.00, 0.00, 0.00, '2024-02-20 17:00:00', 1, 1),
('UI/UX Design for Mobile', 'Create mobile-first design system', 'IN_PROGRESS', 'HIGH', 2, 6, 2, 24.00, 8.00, 30.00, '2024-02-10 17:00:00', 1, 1),

('Data Migration Script', 'Write scripts to migrate data from old to new database', 'DONE', 'HIGH', 3, 4, 3, 20.00, 18.00, 100.00, '2024-01-15 17:00:00', 1, 1),
('Performance Testing', 'Test database performance after migration', 'IN_PROGRESS', 'MEDIUM', 3, 5, 3, 16.00, 10.00, 65.00, '2024-02-01 17:00:00', 1, 1),
('Backup Strategy Implementation', 'Implement automated backup procedures', 'REVIEW', 'HIGH', 3, 6, 3, 12.00, 12.00, 90.00, '2024-01-25 17:00:00', 1, 1),

('Penetration Testing', 'Conduct comprehensive penetration testing', 'DONE', 'URGENT', 4, 4, 2, 32.00, 30.00, 100.00, '2024-01-20 17:00:00', 1, 1),
('Security Report', 'Compile detailed security audit report', 'DONE', 'HIGH', 4, 5, 2, 16.00, 14.00, 100.00, '2024-01-30 17:00:00', 1, 1),
('Vulnerability Fixes', 'Implement fixes for identified vulnerabilities', 'DONE', 'URGENT', 4, 6, 2, 24.00, 22.00, 100.00, '2024-01-31 17:00:00', 1, 1),

('Training Material Preparation', 'Prepare training materials and presentations', 'TODO', 'LOW', 5, 4, 3, 20.00, 0.00, 0.00, '2024-03-15 17:00:00', 1, 1),
('Schedule Training Sessions', 'Coordinate with teams to schedule training', 'TODO', 'LOW', 5, 5, 3, 8.00, 0.00, 0.00, '2024-03-10 17:00:00', 1, 1);

-- Insert sample status history
INSERT INTO status_history (entity_type, entity_id, old_status, new_status, changed_by, changed_at, notes) VALUES
('TASK', 1, 'TODO', 'IN_PROGRESS', 4, '2024-01-16 09:00:00', 'Started working on homepage mockup'),
('TASK', 1, 'IN_PROGRESS', 'REVIEW', 4, '2024-01-24 16:30:00', 'Completed initial design, ready for review'),
('TASK', 1, 'REVIEW', 'DONE', 2, '2024-01-25 10:15:00', 'Design approved, moving to implementation'),
('TASK', 3, 'TODO', 'IN_PROGRESS', 6, '2024-01-16 10:00:00', 'Setting up development environment'),
('TASK', 3, 'IN_PROGRESS', 'DONE', 6, '2024-01-19 15:30:00', 'Development environment ready'),
('TASK', 9, 'TODO', 'IN_PROGRESS', 4, '2024-01-10 09:00:00', 'Started data migration script development'),
('TASK', 9, 'IN_PROGRESS', 'DONE', 4, '2024-01-14 17:00:00', 'Migration script completed and tested'),
('PROJECT', 4, 'ACTIVE', 'COMPLETED', 2, '2024-01-31 18:00:00', 'Security audit completed successfully');

-- Create stored procedures for common operations

DELIMITER //

-- Procedure to assign task to user
CREATE PROCEDURE AssignTask(
    IN task_id BIGINT,
    IN user_id BIGINT,
    IN assigner_id BIGINT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    UPDATE tasks 
    SET assigned_user_id = user_id, 
        assigner_id = assigner_id,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = assigner_id
    WHERE id = task_id;
    
    INSERT INTO status_history (entity_type, entity_id, old_status, new_status, changed_by, notes)
    VALUES ('TASK', task_id, 'UNASSIGNED', 'ASSIGNED', assigner_id, CONCAT('Task assigned to user ID: ', user_id));
    
    COMMIT;
END //

-- Procedure to update task status
CREATE PROCEDURE UpdateTaskStatus(
    IN task_id BIGINT,
    IN new_status VARCHAR(50),
    IN user_id BIGINT,
    IN notes TEXT
)
BEGIN
    DECLARE old_status VARCHAR(50);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    SELECT status INTO old_status FROM tasks WHERE id = task_id;
    
    UPDATE tasks 
    SET status = new_status,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = user_id,
        completed_at = CASE WHEN new_status = 'DONE' THEN CURRENT_TIMESTAMP ELSE completed_at END
    WHERE id = task_id;
    
    INSERT INTO status_history (entity_type, entity_id, old_status, new_status, changed_by, notes)
    VALUES ('TASK', task_id, old_status, new_status, user_id, notes);
    
    COMMIT;
END //

-- Function to calculate project progress
CREATE FUNCTION CalculateProjectProgress(project_id BIGINT)
RETURNS DECIMAL(5,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE total_tasks INT DEFAULT 0;
    DECLARE completed_tasks INT DEFAULT 0;
    DECLARE progress DECIMAL(5,2) DEFAULT 0.00;
    
    SELECT COUNT(*) INTO total_tasks FROM tasks WHERE project_id = project_id;
    
    IF total_tasks > 0 THEN
        SELECT COUNT(*) INTO completed_tasks FROM tasks WHERE project_id = project_id AND status = 'DONE';
        SET progress = (completed_tasks / total_tasks) * 100;
    END IF;
    
    RETURN progress;
END //

DELIMITER ;

-- Create triggers to automatically update project progress
DELIMITER //

CREATE TRIGGER update_project_progress_after_task_update
AFTER UPDATE ON tasks
FOR EACH ROW
BEGIN
    IF NEW.project_id IS NOT NULL AND (OLD.status != NEW.status OR OLD.progress_percentage != NEW.progress_percentage) THEN
        UPDATE projects 
        SET progress_percentage = CalculateProjectProgress(NEW.project_id),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.project_id;
    END IF;
END //

CREATE TRIGGER update_project_progress_after_task_insert
AFTER INSERT ON tasks
FOR EACH ROW
BEGIN
    IF NEW.project_id IS NOT NULL THEN
        UPDATE projects 
        SET progress_percentage = CalculateProjectProgress(NEW.project_id),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.project_id;
    END IF;
END //

CREATE TRIGGER update_project_progress_after_task_delete
AFTER DELETE ON tasks
FOR EACH ROW
BEGIN
    IF OLD.project_id IS NOT NULL THEN
        UPDATE projects 
        SET progress_percentage = CalculateProjectProgress(OLD.project_id),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = OLD.project_id;
    END IF;
END //

DELIMITER ;

-- Create indexes for better performance
CREATE INDEX idx_tasks_status_priority ON tasks(status, priority);
CREATE INDEX idx_tasks_due_date_status ON tasks(due_date, status);
CREATE INDEX idx_projects_status_priority ON projects(status, priority);
CREATE INDEX idx_users_role_active ON users(role, is_active);
CREATE INDEX idx_status_history_entity_date ON status_history(entity_type, entity_id, changed_at);

-- Grant permissions (adjust as needed for your environment)
-- GRANT ALL PRIVILEGES ON task_manager.* TO 'task_manager_user'@'localhost' IDENTIFIED BY 'secure_password';
-- FLUSH PRIVILEGES;

SELECT 'Database initialization completed successfully!' AS message;