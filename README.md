# Task Manager Application - Dokumentasi OOP

## 1. Judul dan Deskripsi Program

**Judul**: Task Manager Application

**Deskripsi**:
Task Manager adalah aplikasi manajemen tugas berbasis Java Swing yang dirancang untuk penggunaan internal perusahaan/organisasi. Aplikasi ini memungkinkan administrator dan manajer untuk mengelola proyek, menugaskan tugas kepada karyawan, dan melacak progress pekerjaan dengan menerapkan konsep Object-Oriented Programming (OOP) secara komprehensif.

**Fitur Utama**:

- Manajemen User dengan role-based access control (Admin, Manager, Employee)
- Manajemen Proyek (create, read, update, delete)
- Manajemen Tugas dengan assignment dan tracking
- Dashboard overview dengan statistik
- Authentication dan authorization system
- Database integration dengan MySQL

## 2. Desain Class (Class Diagram)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              TASK MANAGER CLASS DIAGRAM                     │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   <<abstract>>  │    │  <<interface>>  │    │  <<interface>>  │
│   BaseEntity    │    │   Assignable    │    │   Trackable     │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ # id: Long      │    │ + assign()      │    │ + updateStatus()│
│ # createdAt     │    │ + unassign()    │    │ + getHistory()  │
│ # updatedAt     │    │ + isAssigned()  │    │ + isUpdatable() │
│ # createdBy     │    └─────────────────┘    └─────────────────┘
│ # updatedBy     │              ▲                      ▲
├─────────────────┤              │                      │
│ + getId()       │              │                      │
│ + setId()       │              │                      │
│ + abstract      │              │                      │
│   isValid()     │              │                      │
└─────────────────┘              │                      │
         ▲                       │                      │
         │                       │                      │
    ┌────┴────┐              ┌───┴───┐              ┌───┴───┐
    │  User   │              │ Task  │              │Project│
    ├─────────┤              ├───────┤              ├───────┤
    │-username│              │-title │              │-name  │
    │-password│              │-desc  │              │-desc  │
    │-email   │              │-status│              │-status│
    │-role    │              │-prior │              │-creator│
    │-fullName│              │-proj  │              └───────┘
    ├─────────┤              │-assign│                  │
    │+getRole()│              │-due   │                  │
    │+setRole()│              ├───────┤                  │
    │+verify  │              │+assign│                  │
    │Password()│              │+update│                  │
    └─────────┘              │Status │                  │
         │                   └───────┘                  │
         │                       │                      │
         └───────────────────────┼──────────────────────┘
                                 │
┌─────────────────────────────────┼─────────────────────────────────┐
│                    DAO LAYER    │                                 │
│                                 ▼                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐│
│  │  <<abstract>>   │    │    UserDAO      │    │   ProjectDAO    ││
│  │    BaseDAO<T>   │    ├─────────────────┤    ├─────────────────┤│
│  ├─────────────────┤    │ + findByUsername│    │ + findByCreator ││
│  │ + save()        │    │ + authenticate  │    │ + findByStatus  ││
│  │ + findById()    │    │ + updatePassword│    │ + updateStatus  ││
│  │ + findAll()     │    └─────────────────┘    └─────────────────┘│
│  │ + deleteById()  │              ▲                      ▲        │
│  │ # abstract      │              │                      │        │
│  │   getTableName()│              │                      │        │
│  │ # abstract      │              │                      │        │
│  │   mapResultSet()│              │                      │        │
│  └─────────────────┘              │                      │        │
│           ▲                       │                      │        │
│           └───────────────────────┼──────────────────────┘        │
│                                   │                               │
│                           ┌─────────────────┐                     │
│                           │    TaskDAO      │                     │
│                           ├─────────────────┤                     │
│                           │ + findByProject │                     │
│                           │ + findByAssigned│                     │
│                           │ + findByStatus  │                     │
│                           │ + updateStatus  │                     │
│                           └─────────────────┘                     │
└─────────────────────────────────────────────────────────────────────┘
```

## 3. Penjelasan Objek, Method, dan Variabel (State)

### Objek Utama:

**User Object**:

- **State**: username, password, email, role, fullName
- **Behavior**: authenticate, verifyPassword, setRole
- **Hubungan**: User dapat memiliki banyak Task (assigned), User dapat membuat banyak Project

**Project Object**:

- **State**: name, description, status, creator
- **Behavior**: updateStatus, isUpdatable, assign creator
- **Hubungan**: Project memiliki banyak Task, Project dimiliki oleh satu User (creator)

**Task Object**:

- **State**: title, description, status, priority, assignedUser, project, dueDate
- **Behavior**: assign, unassign, updateStatus, isAssigned
- **Hubungan**: Task dimiliki oleh satu Project, Task dapat di-assign ke satu User

### Method Utama:

| Class   | Method                             | Fungsi                          |
| ------- | ---------------------------------- | ------------------------------- |
| User    | `authenticate(username, password)` | Validasi login user             |
| User    | `verifyPassword(password)`         | Verifikasi password dengan hash |
| Task    | `assign(user, assigner)`           | Menugaskan task ke user         |
| Task    | `updateStatus(status, updatedBy)`  | Update status task              |
| Project | `updateStatus(status, updatedBy)`  | Update status project           |
| BaseDAO | `save(entity)`                     | Simpan atau update entity       |
| BaseDAO | `findById(id)`                     | Cari entity berdasarkan ID      |

### Variabel Instance (State):

**BaseEntity**:

- `protected Long id` - Primary key
- `protected LocalDateTime createdAt` - Timestamp pembuatan
- `protected LocalDateTime updatedAt` - Timestamp update terakhir
- `protected Long createdBy` - ID user pembuat
- `protected Long updatedBy` - ID user yang terakhir update

## 4. Enkapsulasi

### Penggunaan Access Modifier:

**Private Fields dengan Public Getter/Setter**:

```java
public class User extends BaseEntity {
    private String username;        // Private - hanya bisa diakses dalam class
    private String password;        // Private - sensitive data
    private String email;          // Private - data protection
    private Role role;             // Private - controlled access

    // Public getter dengan validation
    public String getUsername() {
        return username;
    }

    // Public setter dengan validation
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        this.username = username.trim();
    }
}
```

**Protected Fields untuk Inheritance**:

```java
public abstract class BaseEntity {
    protected Long id;              // Protected - akses untuk subclass
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected Long createdBy;
    protected Long updatedBy;

    // Protected method untuk subclass
    protected void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

**Package-Private untuk Utility**:

```java
class DatabaseUtils {               // Package-private class
    static String formatDate(LocalDateTime date) {  // Package-private method
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
```

## 5. Inheritance

### Hubungan Pewarisan:

| Superclass | Subclass   | Relationship Type | Inherited Members                |
| ---------- | ---------- | ----------------- | -------------------------------- |
| BaseEntity | User       | IS-A              | id, timestamps, CRUD methods     |
| BaseEntity | Project    | IS-A              | id, timestamps, CRUD methods     |
| BaseEntity | Task       | IS-A              | id, timestamps, CRUD methods     |
| BaseDAO<T> | UserDAO    | IS-A              | save(), findById(), generic CRUD |
| BaseDAO<T> | ProjectDAO | IS-A              | save(), findById(), generic CRUD |
| BaseDAO<T> | TaskDAO    | IS-A              | save(), findById(), generic CRUD |

### Method Override Example:

```java
// Di BaseEntity (Superclass)
public abstract class BaseEntity {
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }

    public abstract boolean isValid();
}

// Di User (Subclass) - Override
public class User extends BaseEntity {
    @Override
    public String toString() {
        return "User[id=" + getId() + ", username=" + username + "]";
    }

    @Override
    public boolean isValid() {
        return username != null && !username.isEmpty() &&
               email != null && email.contains("@") &&
               password != null && password.length() >= 6;
    }
}

// Di Task (Subclass) - Override
public class Task extends BaseEntity {
    @Override
    public boolean isValid() {
        return title != null && !title.isEmpty() &&
               project != null &&
               status != null;
    }
}
```

### IS-A dan HAS-A Relationships:

**IS-A Relationships (Inheritance)**:

- User IS-A BaseEntity
- Project IS-A BaseEntity
- Task IS-A BaseEntity
- UserDAO IS-A BaseDAO

**HAS-A Relationships (Composition/Aggregation)**:

- Task HAS-A User (assignedUser)
- Task HAS-A Project
- Task HAS-A User (assigner)
- Project HAS-A User (creator)
- UserDAO HAS-A DatabaseManager

## 6. Tingkat Akses

### Penggunaan Access Level:

| Modifier      | Penggunaan                          | Alasan                                | Contoh                    |
| ------------- | ----------------------------------- | ------------------------------------- | ------------------------- |
| **private**   | Fields sensitif, helper methods     | Enkapsulasi data, hide implementation | `private String password` |
| **protected** | BaseEntity fields, template methods | Akses untuk subclass inheritance      | `protected Long id`       |
| **public**    | API methods, constructors           | Interface untuk client code           | `public void save()`      |
| **default**   | Package utilities, internal classes | Akses dalam package yang sama         | `class DatabaseUtils`     |

### Contoh Implementasi:

```java
public class User extends BaseEntity {
    // Private - hanya dalam class ini
    private String password;
    private String hashPassword(String plainText) { ... }

    // Protected - untuk subclass (jika ada)
    protected void validateEmail(String email) { ... }

    // Public - interface untuk client
    public boolean verifyPassword(String password) { ... }
    public String getUsername() { ... }

    // Package-private - untuk testing atau utility
    String getHashedPassword() { return password; }
}
```

## 7. Polimorfisme

### Referensi dan Objek Polimorfik:

```java
// Referensi superclass, objek subclass
BaseEntity entity1 = new User();
BaseEntity entity2 = new Project();
BaseEntity entity3 = new Task();

// Array polimorfik
BaseEntity[] entities = {
    new User("john", "password", "john@email.com", Role.EMPLOYEE, "John Doe"),
    new Project("Website", "Company website", Status.ACTIVE),
    new Task("Design UI", "Create user interface", Priority.HIGH)
};

// Method overriding - runtime polymorphism
for (BaseEntity entity : entities) {
    System.out.println(entity.toString());  // Calls overridden toString()
    System.out.println(entity.isValid());   // Calls overridden isValid()
}
```

### Interface Polymorphism:

```java
// Polimorfisme melalui interface
Assignable assignableTask = new Task();
Trackable trackableProject = new Project();
Trackable trackableTask = new Task();

// Runtime polymorphism
assignableTask.assign(user, manager);     // Calls Task.assign()
trackableProject.updateStatus("COMPLETED", user); // Calls Project.updateStatus()
trackableTask.updateStatus("IN_PROGRESS", user);  // Calls Task.updateStatus()
```

### Method Overloading:

```java
public class TaskDAO extends BaseDAO<Task> {
    // Overloaded methods - compile-time polymorphism
    public List<Task> findByStatus(Task.Status status) {
        return findByStatus(status.name());
    }

    public List<Task> findByStatus(String status) {
        // Implementation with string parameter
    }

    public List<Task> findByStatus(Task.Status status, int limit) {
        // Implementation with limit
    }

    public List<Task> findByStatus(Task.Status status, User assignedUser) {
        // Implementation with assigned user filter
    }
}
```

## 8. Abstract Class dan Method

### Abstract Classes:

**BaseEntity (Abstract Class)**:

```java
public abstract class BaseEntity {
    protected Long id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // Concrete methods - shared implementation
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // Abstract method - must be implemented by subclasses
    public abstract boolean isValid();

    // Template method pattern
    public final boolean saveIfValid() {
        if (isValid()) {
            updateTimestamp();
            return true;
        }
        return false;
    }
}
```

**BaseDAO (Abstract Class)**:

```java
public abstract class BaseDAO<T extends BaseEntity> {
    protected DatabaseManager dbManager;

    // Template method - defines algorithm structure
    public boolean save(T entity) {
        if (entity.getId() == null) {
            return insert(entity);  // Calls abstract method
        } else {
            return update(entity);  // Calls abstract method
        }
    }

    // Abstract methods - implemented by subclasses
    protected abstract String getTableName();
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity);
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity);
}
```

### Alasan Penggunaan Abstract:

1. **Code Reuse**: Menghindari duplikasi kode common functionality
2. **Template Method Pattern**: Mendefinisikan algoritma umum, detail implementasi di subclass
3. **Enforce Contract**: Memaksa subclass mengimplementasikan method tertentu
4. **Partial Implementation**: Menyediakan sebagian implementasi yang dapat digunakan subclass

### Implementasi di Subclass:

```java
public class UserDAO extends BaseDAO<User> {
    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("email"),
            User.Role.valueOf(rs.getString("role")),
            rs.getString("full_name")
        );
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO users (username, password_hash, email, role, full_name) VALUES (?, ?, ?, ?, ?)";
    }
}
```

## 9. Interface

### Interface Definitions:

**Assignable Interface**:

```java
public interface Assignable {
    void assign(User user, User assigner);
    void unassign();
    User getAssignedUser();
    User getAssigner();
    boolean isAssigned();
    LocalDateTime getAssignedAt();
}
```

**Trackable Interface**:

```java
public interface Trackable {
    String getCurrentStatus();
    void updateStatus(String newStatus, User updatedBy);
    List<String> getStatusHistory();
    LocalDateTime getLastUpdated();
    boolean isUpdatable();
    User getLastUpdatedBy();
}
```

### Implementation di Task Class:

```java
public class Task extends BaseEntity implements Assignable, Trackable {
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private User assignedUser;
    private User assigner;
    private Project project;
    private LocalDateTime dueDate;
    private LocalDateTime assignedAt;

    // Implementasi Assignable interface
    @Override
    public void assign(User user, User assigner) {
        this.assignedUser = user;
        this.assigner = assigner;
        this.assignedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = assigner.getId();
    }

    @Override
    public void unassign() {
        this.assignedUser = null;
        this.assigner = null;
        this.assignedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean isAssigned() {
        return assignedUser != null;
    }

    // Implementasi Trackable interface
    @Override
    public void updateStatus(String newStatus, User updatedBy) {
        this.status = Status.valueOf(newStatus);
        this.updatedBy = updatedBy.getId();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getCurrentStatus() {
        return status.name();
    }

    @Override
    public boolean isUpdatable() {
        return status != Status.COMPLETED && status != Status.CANCELLED;
    }
}
```

### Implementation di Project Class:

```java
public class Project extends BaseEntity implements Trackable {
    private String name;
    private String description;
    private Status status;
    private User creator;

    // Implementasi Trackable interface
    @Override
    public void updateStatus(String newStatus, User updatedBy) {
        this.status = Status.valueOf(newStatus);
        this.updatedBy = updatedBy.getId();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getCurrentStatus() {
        return status.name();
    }

    @Override
    public boolean isUpdatable() {
        return status == Status.ACTIVE || status == Status.PAUSED;
    }
}
```

### Fungsi Interface dalam Program:

1. **Multiple Inheritance**: Java tidak support multiple class inheritance, tapi bisa implement multiple interfaces
2. **Contract Definition**: Mendefinisikan kontrak yang harus dipenuhi implementing class
3. **Loose Coupling**: Mengurangi ketergantungan antar class
4. **Polymorphism**: Memungkinkan polimorfisme melalui interface reference
5. **Flexibility**: Memudahkan penambahan fitur baru tanpa mengubah existing code

## 10. Desain dan Implementasi Database

### Entity Relationship Diagram:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              DATABASE SCHEMA                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     USERS       │    │    PROJECTS     │    │     TASKS       │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ id (PK)         │◄──┐│ id (PK)         │◄──┐│ id (PK)         │
│ username        │   ││ name            │   ││ title           │
│ password_hash   │   ││ description     │   ││ description     │
│ email           │   ││ status          │   ││ status          │
│ full_name       │   ││ created_by (FK) │───┘│ priority        │
│ role            │   ││ created_at      │    │ project_id (FK) │───┐
│ created_at      │   ││ updated_at      │    │ assigned_user_id│───┼──┐
│ updated_at      │   ││ created_by      │    │ assigner_id (FK)│───┼──┼──┐
│ created_by      │   ││ updated_by      │    │ due_date        │   │  │  │
│ updated_by      │   │└─────────────────┘    │ created_at      │   │  │  │
└─────────────────┘   │                       │ updated_at      │   │  │  │
         ▲             │                       │ created_by      │   │  │  │
         │             │                       │ updated_by      │   │  │  │
         └─────────────┼───────────────────────└─────────────────┘   │  │  │
                       │                                ▲            │  │  │
                       │                                └────────────┘  │  │
                       │                                                 │  │
                       │                                ┌────────────────┘  │
                       │                                │                   │
                       │                                ▼                   │
                       │                       ┌─────────────────┐          │
                       │                       │ STATUS_HISTORY  │          │
                       │                       ├─────────────────┤          │
                       │                       │ id (PK)         │          │
                       │                       │ entity_type     │          │
                       │                       │ entity_id       │          │
                       │                       │ old_status      │          │
                       │                       │ new_status      │          │
                       │                       │ changed_by (FK) │──────────┘
                       │                       │ changed_at      │
                       │                       │ notes           │
                       │                       └─────────────────┘
                       │                                ▲
                       └────────────────────────────────┘
```

### Koneksi Database (JDBC):

```java
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/task_manager";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            createDatabaseIfNotExists();
            createTables();
            insertDefaultData();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return connection;
    }

    private void createTables() {
        try {
            Statement stmt = connection.createStatement();

            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password_hash VARCHAR(255) NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "full_name VARCHAR(100) NOT NULL," +
                "role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "created_by BIGINT," +
                "updated_by BIGINT," +
                "INDEX idx_username (username)," +
                "INDEX idx_email (email)," +
                "INDEX idx_role (role)" +
                ")";

            stmt.execute(createUsersTable);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables", e);
        }
    }
}
```

### Query Examples:

```java
// UserDAO - Authentication
public User authenticate(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ?";
    try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            User user = mapResultSetToEntity(rs);
            if (user.verifyPassword(password)) {
                return user;
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("Authentication failed", e);
    }
    return null;
}

// TaskDAO - Find by assigned user with JOIN
public List<Task> findByAssignedUser(Long userId) throws SQLException {
    String sql = """SELECT t.*, p.name as project_name,
                           u1.username as assigned_username,
                           u2.username as assigner_username
                    FROM tasks t
                    LEFT JOIN projects p ON t.project_id = p.id
                    LEFT JOIN users u1 ON t.assigned_user_id = u1.id
                    LEFT JOIN users u2 ON t.assigner_id = u2.id
                    WHERE t.assigned_user_id = ?
                    ORDER BY t.created_at DESC""";

    List<Task> tasks = new ArrayList<>();
    try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
        stmt.setLong(1, userId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Task task = mapResultSetToEntity(rs);
            // Set additional data from JOIN
            task.setProjectName(rs.getString("project_name"));
            tasks.add(task);
        }
    }
    return tasks;
}

// ProjectDAO - Complex query with aggregation
public List<ProjectSummary> getProjectSummaries() throws SQLException {
    String sql = """SELECT p.id, p.name, p.status,
                           COUNT(t.id) as total_tasks,
                           COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completed_tasks,
                           u.username as creator_name
                    FROM projects p
                    LEFT JOIN tasks t ON p.id = t.project_id
                    LEFT JOIN users u ON p.created_by = u.id
                    GROUP BY p.id, p.name, p.status, u.username
                    ORDER BY p.created_at DESC""";

    // Implementation...
}
```

### Fungsi Setiap Tabel:

| Tabel              | Fungsi                                                       | Key Columns                                                       | Relationships                                                                |
| ------------------ | ------------------------------------------------------------ | ----------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| **users**          | Menyimpan data pengguna, authentication, dan role management | id (PK), username (UNIQUE), email (UNIQUE)                        | Referenced by projects.created_by, tasks.assigned_user_id, tasks.assigner_id |
| **projects**       | Menyimpan informasi proyek dan hubungan dengan creator       | id (PK), created_by (FK to users)                                 | Has many tasks, belongs to user (creator)                                    |
| **tasks**          | Menyimpan tugas, assignment, dan hubungan dengan project     | id (PK), project_id (FK), assigned_user_id (FK), assigner_id (FK) | Belongs to project, assigned to user, assigned by user                       |
| **status_history** | Audit trail untuk perubahan status project/task              | entity_type, entity_id, changed_by (FK to users)                  | Polymorphic relationship to projects/tasks                                   |

### Database Constraints dan Indexes:

```sql
-- Foreign Key Constraints
ALTER TABLE projects ADD CONSTRAINT fk_projects_creator
    FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_project
    FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_assigned_user
    FOREIGN KEY (assigned_user_id) REFERENCES users(id);

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_assigner
    FOREIGN KEY (assigner_id) REFERENCES users(id);

-- Performance Indexes
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_status_history_entity ON status_history(entity_type, entity_id);
```

---

**Developed by**: Razaqa Albio Kasyfi & Naufal  
**Course**: Pemrograman Berorientasi Objek  
**Institution**: Universitas Brawijaya Fakultas Vokasi  
**Year**: 2025
