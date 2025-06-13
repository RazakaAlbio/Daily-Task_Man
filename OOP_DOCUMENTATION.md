# Dokumentasi Implementasi OOP - Task Manager Application

## 📚 Daftar Isi
1. [Penjelasan Objek, Method, dan Variabel](#1-penjelasan-objek-method-dan-variabel)
2. [Enkapsulasi](#2-enkapsulasi)
3. [Inheritance (Pewarisan)](#3-inheritance-pewarisan)
4. [Tingkat Akses](#4-tingkat-akses)
5. [Polimorfisme](#5-polimorfisme)
6. [Abstract Class dan Method](#6-abstract-class-dan-method)
7. [Interface](#7-interface)
8. [Hubungan IS-A dan HAS-A](#8-hubungan-is-a-dan-has-a)

---

## 1. Penjelasan Objek, Method, dan Variabel

### 1.1 Objek Utama dalam Sistem

#### **User Object**
```java
public class User extends BaseEntity {
    // Instance Variables (State)
    private String username;        // Nama pengguna unik
    private String passwordHash;    // Hash password untuk keamanan
    private String email;          // Email pengguna
    private String fullName;       // Nama lengkap
    private Role role;             // Peran dalam sistem
    private boolean isActive;      // Status aktif pengguna
    
    // Methods (Behavior)
    public boolean authenticate(String password) { ... }
    public boolean hasPermission(String action) { ... }
    public void updateProfile(String email, String fullName) { ... }
}
```

**Fungsi**: Merepresentasikan pengguna sistem dengan berbagai peran (Admin, Manager, Employee)

#### **Project Object**
```java
public class Project extends BaseEntity implements Trackable {
    // Instance Variables
    private String name;              // Nama proyek
    private String description;       // Deskripsi proyek
    private Status status;           // Status proyek
    private Priority priority;       // Prioritas proyek
    private User creator;            // Pembuat proyek
    private LocalDate startDate;     // Tanggal mulai
    private LocalDate endDate;       // Tanggal selesai
    private double progressPercentage; // Persentase kemajuan
    
    // Methods
    public void updateProgress() { ... }
    public List<Task> getTasks() { ... }
    public boolean isOverdue() { ... }
}
```

**Fungsi**: Mengelola informasi proyek dan melacak kemajuan

#### **Task Object**
```java
public class Task extends BaseEntity implements Assignable, Trackable {
    // Instance Variables
    private String title;            // Judul tugas
    private String description;      // Deskripsi tugas
    private Status status;          // Status tugas
    private Priority priority;      // Prioritas tugas
    private Project project;        // Proyek terkait
    private User assignedUser;      // Pengguna yang ditugaskan
    private User assigner;          // Pengguna yang menugaskan
    private LocalDateTime dueDate;  // Batas waktu
    
    // Methods
    public void assign(User user, User assigner) { ... }
    public void updateStatus(String newStatus, User updatedBy) { ... }
    public boolean isOverdue() { ... }
}
```

**Fungsi**: Merepresentasikan tugas individual yang dapat ditugaskan dan dilacak

### 1.2 Method Utama dan Fungsinya

| Method | Class | Fungsi |
|--------|-------|--------|
| `authenticate()` | User | Memverifikasi kredensial pengguna |
| `save()` | BaseDAO | Template method untuk menyimpan entity |
| `assign()` | Task | Menugaskan task kepada user |
| `updateStatus()` | Task/Project | Mengubah status dan mencatat history |
| `findByStatus()` | TaskDAO | Mencari task berdasarkan status |
| `getConnection()` | DatabaseManager | Mendapatkan koneksi database |
| `showPanel()` | GUI Classes | Menampilkan panel GUI |

### 1.3 Variabel Instance (State) Penting

| Variabel | Type | Scope | Fungsi |
|----------|------|-------|--------|
| `id` | Long | protected | Primary key untuk semua entity |
| `createdAt` | LocalDateTime | protected | Timestamp pembuatan |
| `updatedAt` | LocalDateTime | protected | Timestamp update terakhir |
| `currentUser` | User | private | User yang sedang login |
| `connection` | Connection | private | Koneksi database aktif |
| `status` | Enum | private | Status entity (Task/Project) |

---

## 2. Enkapsulasi

### 2.1 Private Fields dengan Public Accessors

```java
public class User extends BaseEntity {
    // PRIVATE fields - tidak dapat diakses langsung dari luar class
    private String username;
    private String passwordHash;
    private String email;
    private Role role;
    
    // PUBLIC getters - controlled access untuk membaca data
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    // PUBLIC setters dengan validasi - controlled access untuk menulis data
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username tidak boleh kosong");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username harus 3-50 karakter");
        }
        this.username = username.trim().toLowerCase();
    }
    
    public void setEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Format email tidak valid");
        }
        this.email = email.toLowerCase();
    }
    
    // PRIVATE helper method - implementation detail yang disembunyikan
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
```

### 2.2 Enkapsulasi dalam Database Operations

```java
public class UserDAO extends BaseDAO<User> {
    // PRIVATE connection details - disembunyikan dari client
    private static final String TABLE_NAME = "users";
    private static final String INSERT_SQL = "INSERT INTO users (username, password_hash, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
    
    // PUBLIC interface - hanya expose method yang diperlukan
    public User findByUsername(String username) throws SQLException {
        return executeQuery("SELECT * FROM users WHERE username = ?", username);
    }
    
    public boolean existsByEmail(String email) throws SQLException {
        return executeQuery("SELECT COUNT(*) FROM users WHERE email = ?", email) > 0;
    }
    
    // PRIVATE implementation - detail query disembunyikan
    private User executeQuery(String sql, Object... params) throws SQLException {
        // Implementation details hidden from client
    }
}
```

### 2.3 Manfaat Enkapsulasi

1. **Data Protection**: Field private melindungi data dari akses tidak sah
2. **Validation**: Setter dapat memvalidasi data sebelum disimpan
3. **Flexibility**: Implementation dapat diubah tanpa mempengaruhi client code
4. **Maintainability**: Perubahan internal tidak mempengaruhi kode lain

---

## 3. Inheritance (Pewarisan)

### 3.1 Hierarki Inheritance

```
                    BaseEntity (Abstract)
                         |
        +----------------+----------------+
        |                |                |
      User           Project           Task
        |                |                |
   (concrete)      (concrete)      (concrete)
        |                |                |
   implements      implements      implements
        |                |                |
        -           Trackable    Assignable + Trackable
```

### 3.2 BaseEntity - Superclass

```java
public abstract class BaseEntity {
    // PROTECTED fields - dapat diakses oleh subclass
    protected Long id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected Long createdBy;
    protected Long updatedBy;
    
    // CONCRETE methods - diwariskan ke semua subclass
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // TEMPLATE method - menggunakan abstract method
    public boolean save() {
        if (!isValid()) {
            throw new IllegalStateException("Entity tidak valid");
        }
        updateTimestamps();
        return true;
    }
    
    // ABSTRACT method - harus diimplementasi oleh subclass
    public abstract boolean isValid();
    
    // PROTECTED helper method
    protected void updateTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        if (this.id == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }
    
    // Method yang dapat di-override
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
}
```

### 3.3 Subclass Implementation

#### User Class
```java
public class User extends BaseEntity {
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private Role role;
    
    // OVERRIDE abstract method dari BaseEntity
    @Override
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               email != null && email.contains("@") &&
               passwordHash != null && !passwordHash.isEmpty() &&
               role != null;
    }
    
    // OVERRIDE toString method
    @Override
    public String toString() {
        return "User[id=" + getId() + ", username=" + username + ", role=" + role + "]";
    }
    
    // ADDITIONAL methods specific to User
    public boolean authenticate(String password) {
        return BCrypt.checkpw(password, this.passwordHash);
    }
    
    public boolean hasRole(Role requiredRole) {
        return this.role == requiredRole;
    }
}
```

#### Task Class
```java
public class Task extends BaseEntity implements Assignable, Trackable {
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Project project;
    private User assignedUser;
    private User assigner;
    private LocalDateTime dueDate;
    
    // OVERRIDE abstract method
    @Override
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               status != null &&
               priority != null;
    }
    
    // OVERRIDE toString
    @Override
    public String toString() {
        return "Task[id=" + getId() + ", title=" + title + ", status=" + status + "]";
    }
    
    // IMPLEMENT interface methods
    @Override
    public void assign(User user, User assigner) {
        this.assignedUser = user;
        this.assigner = assigner;
        updateTimestamps(); // Menggunakan method dari BaseEntity
    }
    
    @Override
    public void updateStatus(String newStatus, User updatedBy) {
        this.status = Status.valueOf(newStatus);
        this.updatedBy = updatedBy.getId();
        updateTimestamps();
    }
}
```

### 3.4 DAO Inheritance Hierarchy

```java
// Abstract base class untuk semua DAO
public abstract class BaseDAO<T extends BaseEntity> {
    protected Connection getConnection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }
    
    // TEMPLATE METHOD pattern
    public boolean save(T entity) {
        try {
            if (entity.getId() == null) {
                return insert(entity);
            } else {
                return update(entity);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving entity", e);
        }
    }
    
    // ABSTRACT methods - harus diimplementasi subclass
    protected abstract String getTableName();
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    // CONCRETE methods yang dapat digunakan semua subclass
    public T findById(Long id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapResultSet(rs) : null;
        }
    }
    
    public List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY created_at DESC";
        List<T> results = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add(mapResultSet(rs));
            }
        }
        return results;
    }
}

// Concrete DAO implementation
public class UserDAO extends BaseDAO<User> {
    @Override
    protected String getTableName() {
        return "users";
    }
    
    @Override
    protected User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        // Set timestamps dari BaseEntity
        return user;
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO users (username, password_hash, email, full_name, role, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }
    
    // Additional methods specific to User
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapResultSet(rs) : null;
        }
    }
}
```

---

## 4. Tingkat Akses

### 4.1 Tabel Tingkat Akses

| Modifier | Same Class | Same Package | Subclass | Different Package |
|----------|------------|--------------|----------|-------------------|
| **private** | ✅ | ❌ | ❌ | ❌ |
| **default** | ✅ | ✅ | ❌ | ❌ |
| **protected** | ✅ | ✅ | ✅ | ❌ |
| **public** | ✅ | ✅ | ✅ | ✅ |

### 4.2 Implementasi dalam Kode

#### Private Access
```java
public class User extends BaseEntity {
    // PRIVATE - hanya dapat diakses dalam class User
    private String passwordHash;        // Keamanan: password tidak boleh diakses langsung
    private static final String SALT = "$2a$12$";  // Konstanta internal
    
    // PRIVATE helper methods
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }
    
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, SALT);
    }
    
    // PUBLIC interface untuk mengakses private data
    public boolean authenticate(String password) {
        return BCrypt.checkpw(password, this.passwordHash);
    }
}
```

#### Protected Access
```java
public abstract class BaseEntity {
    // PROTECTED - dapat diakses oleh subclass
    protected Long id;                    // Subclass perlu akses untuk inheritance
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    
    // PROTECTED methods untuk subclass
    protected void updateTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        if (this.id == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }
    
    protected void validateId() {
        if (this.id != null && this.id <= 0) {
            throw new IllegalArgumentException("ID harus positif");
        }
    }
}

// Subclass dapat mengakses protected members
public class Task extends BaseEntity {
    public void markAsCompleted() {
        this.status = Status.DONE;
        updateTimestamps(); // Mengakses protected method dari parent
        validateId();       // Mengakses protected method dari parent
    }
}
```

#### Package-Private (Default)
```java
// File: dao/DatabaseConfig.java
class DatabaseConfig {  // package-private class
    static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/task_manager";
    static final int CONNECTION_TIMEOUT = 30;
    
    // package-private methods
    static Properties loadProperties() {
        // Implementation
    }
    
    static void validateConnection(Connection conn) {
        // Implementation
    }
}

// File: dao/BaseDAO.java (same package)
public abstract class BaseDAO<T extends BaseEntity> {
    protected Connection getConnection() throws SQLException {
        // Dapat mengakses DatabaseConfig karena dalam package yang sama
        Properties props = DatabaseConfig.loadProperties();
        Connection conn = DriverManager.getConnection(
            DatabaseConfig.DEFAULT_URL, props);
        DatabaseConfig.validateConnection(conn);
        return conn;
    }
}
```

#### Public Access
```java
public class TaskManagerApp {
    // PUBLIC - dapat diakses dari mana saja
    public static final String APP_NAME = "Task Manager";
    public static final String VERSION = "1.0.0";
    
    // PUBLIC constructor
    public TaskManagerApp() {
        initializeApplication();
    }
    
    // PUBLIC methods - API untuk client code
    public void start() {
        showLoginPanel();
    }
    
    public void shutdown() {
        cleanup();
        System.exit(0);
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
}
```

### 4.3 Alasan Penggunaan Setiap Modifier

| Modifier | Kapan Digunakan | Alasan |
|----------|-----------------|--------|
| **private** | Fields sensitif, helper methods | Enkapsulasi maksimal, keamanan data |
| **protected** | Base class members untuk inheritance | Memungkinkan subclass access tanpa public exposure |
| **default** | Utility classes, package-internal APIs | Cohesion dalam package, tidak untuk external use |
| **public** | API methods, constructors, constants | Interface yang dapat digunakan client code |

---

## 5. Polimorfisme

### 5.1 Runtime Polymorphism (Method Overriding)

#### Polymorphic References
```java
// Referensi superclass dapat menunjuk ke objek subclass
BaseEntity entity1 = new User();
BaseEntity entity2 = new Project();
BaseEntity entity3 = new Task();

// Array polimorfik
BaseEntity[] entities = {
    new User("john", "john@email.com"),
    new Project("Website Redesign"),
    new Task("Create Homepage")
};

// Polymorphic method calls
for (BaseEntity entity : entities) {
    System.out.println(entity.toString());  // Calls overridden toString()
    System.out.println(entity.isValid());   // Calls overridden isValid()
    
    // Runtime type determination
    if (entity instanceof User) {
        User user = (User) entity;
        System.out.println("User role: " + user.getRole());
    } else if (entity instanceof Task) {
        Task task = (Task) entity;
        System.out.println("Task status: " + task.getStatus());
    }
}
```

#### Method Overriding Examples
```java
// BaseEntity
public abstract class BaseEntity {
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
    
    public abstract boolean isValid();
}

// User - Override toString dan implement isValid
public class User extends BaseEntity {
    @Override
    public String toString() {
        return "User[id=" + getId() + ", username=" + username + 
               ", role=" + role + ", active=" + isActive + "]";
    }
    
    @Override
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               email != null && email.contains("@") &&
               role != null;
    }
}

// Project - Override toString dan implement isValid
public class Project extends BaseEntity {
    @Override
    public String toString() {
        return "Project[id=" + getId() + ", name=" + name + 
               ", status=" + status + ", progress=" + progressPercentage + "%]";
    }
    
    @Override
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               status != null &&
               progressPercentage >= 0 && progressPercentage <= 100;
    }
}

// Task - Override toString dan implement isValid
public class Task extends BaseEntity {
    @Override
    public String toString() {
        return "Task[id=" + getId() + ", title=" + title + 
               ", status=" + status + ", priority=" + priority + "]";
    }
    
    @Override
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               status != null &&
               priority != null;
    }
}
```

### 5.2 Interface Polymorphism

```java
// Interface polymorphism
Assignable[] assignables = {
    new Task("Implement Login"),
    new Task("Design Database"),
    new Task("Write Documentation")
};

User manager = new User("manager", "manager@company.com");
User employee = new User("employee", "employee@company.com");

// Polymorphic method calls through interface
for (Assignable assignable : assignables) {
    assignable.assign(employee, manager);  // Calls Task.assign()
    System.out.println("Assigned: " + assignable.isAssigned());
}

// Trackable interface polymorphism
Trackable[] trackables = {
    new Project("Mobile App"),
    new Task("Create UI Mockup"),
    new Task("Implement Backend API")
};

for (Trackable trackable : trackables) {
    trackable.updateStatus("IN_PROGRESS", manager);  // Polymorphic call
    System.out.println("Status: " + trackable.getCurrentStatus());
}
```

### 5.3 Compile-time Polymorphism (Method Overloading)

#### DAO Method Overloading
```java
public class TaskDAO extends BaseDAO<Task> {
    
    // Method overloading - same name, different parameters
    
    // 1. Find by status (enum)
    public List<Task> findByStatus(Task.Status status) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE status = ?";
        return executeQuery(sql, status.name());
    }
    
    // 2. Find by status (string)
    public List<Task> findByStatus(String status) throws SQLException {
        return findByStatus(Task.Status.valueOf(status.toUpperCase()));
    }
    
    // 3. Find by status with limit
    public List<Task> findByStatus(Task.Status status, int limit) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE status = ? LIMIT ?";
        return executeQuery(sql, status.name(), limit);
    }
    
    // 4. Find by status and priority
    public List<Task> findByStatus(Task.Status status, Task.Priority priority) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE status = ? AND priority = ?";
        return executeQuery(sql, status.name(), priority.name());
    }
    
    // 5. Find by multiple statuses
    public List<Task> findByStatus(Task.Status... statuses) throws SQLException {
        if (statuses.length == 0) return new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("SELECT * FROM tasks WHERE status IN (");
        for (int i = 0; i < statuses.length; i++) {
            sql.append("?");
            if (i < statuses.length - 1) sql.append(", ");
        }
        sql.append(")");
        
        Object[] params = Arrays.stream(statuses)
                               .map(Enum::name)
                               .toArray();
        return executeQuery(sql.toString(), params);
    }
}
```

#### GUI Component Overloading
```java
public class TaskManagerApp {
    
    // Method overloading untuk membuat button dengan berbagai konfigurasi
    
    // 1. Basic button
    public JButton createButton(String text) {
        return createButton(text, null, null);
    }
    
    // 2. Button with action
    public JButton createButton(String text, ActionListener action) {
        return createButton(text, action, null);
    }
    
    // 3. Button with action and icon
    public JButton createButton(String text, ActionListener action, Icon icon) {
        JButton button = new JButton(text, icon);
        if (action != null) {
            button.addActionListener(action);
        }
        styleButton(button);
        return button;
    }
    
    // 4. Button with custom styling
    public JButton createButton(String text, ActionListener action, Color bgColor, Color fgColor) {
        JButton button = createButton(text, action);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        return button;
    }
    
    // Method overloading untuk panel creation
    
    // 1. Basic panel
    public JPanel createPanel() {
        return createPanel(new FlowLayout());
    }
    
    // 2. Panel with layout
    public JPanel createPanel(LayoutManager layout) {
        return createPanel(layout, null);
    }
    
    // 3. Panel with layout and background
    public JPanel createPanel(LayoutManager layout, Color background) {
        JPanel panel = new JPanel(layout);
        if (background != null) {
            panel.setBackground(background);
        }
        return panel;
    }
}
```

### 5.4 Polymorphic Collections

```java
public class EntityManager {
    // Collection yang dapat menyimpan berbagai jenis BaseEntity
    private List<BaseEntity> entities = new ArrayList<>();
    
    public void addEntity(BaseEntity entity) {
        if (entity.isValid()) {  // Polymorphic method call
            entities.add(entity);
        }
    }
    
    public void saveAll() {
        for (BaseEntity entity : entities) {
            // Polymorphic behavior - each entity saves differently
            if (entity instanceof User) {
                new UserDAO().save((User) entity);
            } else if (entity instanceof Project) {
                new ProjectDAO().save((Project) entity);
            } else if (entity instanceof Task) {
                new TaskDAO().save((Task) entity);
            }
        }
    }
    
    public void printAll() {
        for (BaseEntity entity : entities) {
            System.out.println(entity.toString());  // Polymorphic toString()
        }
    }
    
    // Generic method dengan bounded type parameter
    public <T extends BaseEntity> List<T> getEntitiesByType(Class<T> type) {
        return entities.stream()
                      .filter(type::isInstance)
                      .map(type::cast)
                      .collect(Collectors.toList());
    }
}
```

---

## 6. Abstract Class dan Method

### 6.1 BaseEntity - Abstract Class

```java
public abstract class BaseEntity {
    // CONCRETE fields - diwariskan ke semua subclass
    protected Long id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected Long createdBy;
    protected Long updatedBy;
    
    // CONCRETE methods - implementasi default yang dapat digunakan subclass
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // TEMPLATE METHOD - menggunakan abstract method
    public final boolean saveEntity() {
        // Pre-processing
        if (!isValid()) {
            throw new IllegalStateException("Entity tidak valid: " + getValidationErrors());
        }
        
        updateTimestamps();
        
        // Delegate to subclass implementation
        boolean result = performSave();
        
        // Post-processing
        if (result) {
            onSaveSuccess();
        } else {
            onSaveFailure();
        }
        
        return result;
    }
    
    // ABSTRACT METHODS - harus diimplementasi oleh subclass
    
    /**
     * Validasi entity sebelum disimpan
     * @return true jika entity valid
     */
    public abstract boolean isValid();
    
    /**
     * Mendapatkan daftar error validasi
     * @return list error messages
     */
    public abstract List<String> getValidationErrors();
    
    /**
     * Melakukan operasi save yang spesifik untuk setiap entity
     * @return true jika save berhasil
     */
    protected abstract boolean performSave();
    
    /**
     * Mendapatkan nama tabel untuk entity ini
     * @return nama tabel database
     */
    public abstract String getTableName();
    
    // CONCRETE helper methods
    protected void updateTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        if (this.id == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }
    
    protected void onSaveSuccess() {
        System.out.println(getClass().getSimpleName() + " saved successfully with ID: " + id);
    }
    
    protected void onSaveFailure() {
        System.err.println("Failed to save " + getClass().getSimpleName());
    }
    
    // CONCRETE method dengan default implementation yang dapat di-override
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", created=" + createdAt + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseEntity that = (BaseEntity) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### 6.2 BaseDAO - Abstract DAO Class

```java
public abstract class BaseDAO<T extends BaseEntity> {
    
    // CONCRETE method - template method pattern
    public final boolean save(T entity) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            boolean result;
            if (entity.getId() == null) {
                result = insert(entity, conn);
            } else {
                result = update(entity, conn);
            }
            
            if (result) {
                conn.commit();
                afterSave(entity);
            } else {
                conn.rollback();
            }
            
            return result;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error saving entity: " + entity.getClass().getSimpleName(), e);
        }
    }
    
    // CONCRETE method
    public final T findById(Long id) {
        if (id == null) return null;
        
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() ? mapResultSet(rs) : null;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding entity by ID: " + id, e);
        }
    }
    
    // CONCRETE method
    public final List<T> findAll() {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY " + getDefaultOrderBy();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            List<T> results = new ArrayList<>();
            
            while (rs.next()) {
                results.add(mapResultSet(rs));
            }
            
            return results;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all entities", e);
        }
    }
    
    // ABSTRACT METHODS - harus diimplementasi subclass
    
    /**
     * Nama tabel database untuk entity ini
     */
    protected abstract String getTableName();
    
    /**
     * Mapping ResultSet ke object entity
     */
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    
    /**
     * SQL untuk insert entity baru
     */
    protected abstract String getInsertSQL();
    
    /**
     * SQL untuk update entity existing
     */
    protected abstract String getUpdateSQL();
    
    /**
     * Set parameter untuk insert statement
     */
    protected abstract void setInsertParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    /**
     * Set parameter untuk update statement
     */
    protected abstract void setUpdateParameters(PreparedStatement stmt, T entity) throws SQLException;
    
    // CONCRETE methods dengan default implementation
    protected Connection getConnection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
    }
    
    protected String getDefaultOrderBy() {
        return "created_at DESC";
    }
    
    protected void afterSave(T entity) {
        // Hook method - subclass dapat override jika perlu
    }
    
    // PRIVATE helper methods
    private boolean insert(T entity, Connection conn) throws SQLException {
        String sql = getInsertSQL();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(stmt, entity);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
                return true;
            }
            
            return false;
        }
    }
    
    private boolean update(T entity, Connection conn) throws SQLException {
        String sql = getUpdateSQL();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setUpdateParameters(stmt, entity);
            stmt.setLong(stmt.getParameterMetaData().getParameterCount(), entity.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
}
```

### 6.3 Concrete Implementation - UserDAO

```java
public class UserDAO extends BaseDAO<User> {
    
    // IMPLEMENT abstract methods dari BaseDAO
    
    @Override
    protected String getTableName() {
        return "users";
    }
    
    @Override
    protected User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("is_active"));
        
        // Set BaseEntity fields
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        user.setCreatedBy(rs.getLong("created_by"));
        user.setUpdatedBy(rs.getLong("updated_by"));
        
        return user;
    }
    
    @Override
    protected String getInsertSQL() {
        return "INSERT INTO users (username, password_hash, email, full_name, role, is_active, created_at, created_by) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSQL() {
        return "UPDATE users SET username = ?, email = ?, full_name = ?, role = ?, is_active = ?, " +
               "updated_at = ?, updated_by = ? WHERE id = ?";
    }
    
    @Override
    protected void setInsertParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getPasswordHash());
        stmt.setString(3, user.getEmail());
        stmt.setString(4, user.getFullName());
        stmt.setString(5, user.getRole().name());
        stmt.setBoolean(6, user.isActive());
        stmt.setTimestamp(7, Timestamp.valueOf(user.getCreatedAt()));
        stmt.setLong(8, user.getCreatedBy());
    }
    
    @Override
    protected void setUpdateParameters(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getUsername());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getFullName());
        stmt.setString(4, user.getRole().name());
        stmt.setBoolean(5, user.isActive());
        stmt.setTimestamp(6, Timestamp.valueOf(user.getUpdatedAt()));
        stmt.setLong(7, user.getUpdatedBy());
    }
    
    // ADDITIONAL methods specific to User
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE username = ? AND is_active = true";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() ? mapResultSet(rs) : null;
        }
    }
    
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    public List<User> findByRole(User.Role role) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE role = ? AND is_active = true ORDER BY full_name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.name());
            ResultSet rs = stmt.executeQuery();
            
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
            
            return users;
        }
    }
    
    // OVERRIDE hook method
    @Override
    protected void afterSave(User user) {
        System.out.println("User saved: " + user.getUsername() + " (" + user.getRole() + ")");
        
        // Additional logic after save
        if (user.getRole() == User.Role.ADMIN) {
            // Log admin user creation/update
            auditLog("Admin user modified: " + user.getUsername());
        }
    }
    
    private void auditLog(String message) {
        // Implementation for audit logging
        System.out.println("[AUDIT] " + LocalDateTime.now() + ": " + message);
    }
}
```

### 6.4 Alasan Penggunaan Abstract Class

#### 1. **Code Reuse**
- Menghindari duplikasi kode yang sama di multiple class
- Menyediakan implementasi common functionality

#### 2. **Template Method Pattern**
- Mendefinisikan algoritma umum di abstract class
- Detail implementasi diserahkan ke subclass

#### 3. **Enforce Contract**
- Memaksa subclass mengimplementasi method tertentu
- Menjamin konsistensi interface

#### 4. **Partial Implementation**
- Berbeda dengan interface yang hanya kontrak
- Abstract class dapat menyediakan implementasi parsial

---

## 7. Interface

### 7.1 Assignable Interface

```java
/**
 * Interface untuk entity yang dapat ditugaskan (assigned)
 * Diimplementasi oleh Task class
 */
public interface Assignable {
    
    /**
     * Menugaskan entity kepada user
     * @param user User yang akan ditugaskan
     * @param assigner User yang melakukan penugasan
     */
    void assign(User user, User assigner);
    
    /**
     * Membatalkan penugasan
     */
    void unassign();
    
    /**
     * Mendapatkan user yang ditugaskan
     * @return User yang ditugaskan, null jika belum ditugaskan
     */
    User getAssignedUser();
    
    /**
     * Mendapatkan user yang melakukan penugasan
     * @return User yang melakukan penugasan
     */
    User getAssigner();
    
    /**
     * Mengecek apakah sudah ditugaskan
     * @return true jika sudah ditugaskan
     */
    boolean isAssigned();
    
    /**
     * Mengecek apakah user dapat ditugaskan ke entity ini
     * @param user User yang akan dicek
     * @return true jika dapat ditugaskan
     */
    default boolean canBeAssignedTo(User user) {
        return user != null && user.isActive() && 
               (user.getRole() == User.Role.EMPLOYEE || 
                user.getRole() == User.Role.MANAGER);
    }
    
    /**
     * Mendapatkan tanggal penugasan
     * @return LocalDateTime penugasan, null jika belum ditugaskan
     */
    LocalDateTime getAssignedAt();
}
```

### 7.2 Trackable Interface

```java
/**
 * Interface untuk entity yang dapat dilacak statusnya
 * Diimplementasi oleh Project dan Task class
 */
public interface Trackable {
    
    /**
     * Mendapatkan status saat ini
     * @return Status saat ini dalam bentuk string
     */
    String getCurrentStatus();
    
    /**
     * Mengubah status entity
     * @param newStatus Status baru
     * @param updatedBy User yang melakukan perubahan
     */
    void updateStatus(String newStatus, User updatedBy);
    
    /**
     * Mendapatkan history perubahan status
     * @return List history status
     */
    List<StatusHistory> getStatusHistory();
    
    /**
     * Mendapatkan tanggal update terakhir
     * @return LocalDateTime update terakhir
     */
    LocalDateTime getLastUpdated();
    
    /**
     * Mengecek apakah entity dapat diupdate
     * @return true jika dapat diupdate
     */
    boolean isUpdatable();
    
    /**
     * Mendapatkan progress dalam persentase
     * @return Progress 0-100
     */
    default double getProgressPercentage() {
        return 0.0;
    }
    
    /**
     * Mengecek apakah sudah selesai
     * @return true jika status menunjukkan selesai
     */
    default boolean isCompleted() {
        String status = getCurrentStatus();
        return "COMPLETED".equals(status) || "DONE".equals(status);
    }
    
    /**
     * Mengecek apakah sedang aktif/dalam progress
     * @return true jika sedang aktif
     */
    default boolean isActive() {
        String status = getCurrentStatus();
        return "ACTIVE".equals(status) || "IN_PROGRESS".equals(status);
    }
}
```

### 7.3 Implementation di Task Class

```java
public class Task extends BaseEntity implements Assignable, Trackable {
    
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Project project;
    private User assignedUser;
    private User assigner;
    private LocalDateTime assignedAt;
    private LocalDateTime dueDate;
    private double progressPercentage;
    
    // IMPLEMENT Assignable interface
    
    @Override
    public void assign(User user, User assigner) {
        if (!canBeAssignedTo(user)) {
            throw new IllegalArgumentException("User tidak dapat ditugaskan: " + user.getUsername());
        }
        
        if (assigner == null) {
            throw new IllegalArgumentException("Assigner tidak boleh null");
        }
        
        // Check permission
        if (!assigner.hasRole(User.Role.ADMIN) && !assigner.hasRole(User.Role.MANAGER)) {
            throw new SecurityException("Hanya Admin dan Manager yang dapat menugaskan task");
        }
        
        this.assignedUser = user;
        this.assigner = assigner;
        this.assignedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = assigner.getId();
        
        // Log assignment
        System.out.println("Task '" + title + "' assigned to " + user.getUsername() + 
                          " by " + assigner.getUsername());
    }
    
    @Override
    public void unassign() {
        if (!isAssigned()) {
            throw new IllegalStateException("Task belum ditugaskan");
        }
        
        System.out.println("Task '" + title + "' unassigned from " + assignedUser.getUsername());
        
        this.assignedUser = null;
        this.assigner = null;
        this.assignedAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public User getAssignedUser() {
        return assignedUser;
    }
    
    @Override
    public User getAssigner() {
        return assigner;
    }
    
    @Override
    public boolean isAssigned() {
        return assignedUser != null;
    }
    
    @Override
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    // IMPLEMENT Trackable interface
    
    @Override
    public String getCurrentStatus() {
        return status != null ? status.name() : "UNKNOWN";
    }
    
    @Override
    public void updateStatus(String newStatus, User updatedBy) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status tidak boleh kosong");
        }
        
        if (updatedBy == null) {
            throw new IllegalArgumentException("UpdatedBy tidak boleh null");
        }
        
        // Validate status transition
        Status oldStatus = this.status;
        Status newStatusEnum;
        
        try {
            newStatusEnum = Status.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status tidak valid: " + newStatus);
        }
        
        if (!isValidStatusTransition(oldStatus, newStatusEnum)) {
            throw new IllegalStateException("Transisi status tidak valid dari " + 
                                          oldStatus + " ke " + newStatusEnum);
        }
        
        // Update status
        this.status = newStatusEnum;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy.getId();
        
        // Auto-update progress based on status
        updateProgressBasedOnStatus();
        
        // Log status change
        System.out.println("Task '" + title + "' status changed from " + 
                          oldStatus + " to " + newStatusEnum + " by " + updatedBy.getUsername());
    }
    
    @Override
    public List<StatusHistory> getStatusHistory() {
        // Implementation would fetch from database
        // For now, return empty list
        return new ArrayList<>();
    }
    
    @Override
    public LocalDateTime getLastUpdated() {
        return getUpdatedAt();
    }
    
    @Override
    public boolean isUpdatable() {
        return status != Status.DONE && status != Status.CANCELLED;
    }
    
    @Override
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    // HELPER methods
    
    private boolean isValidStatusTransition(Status from, Status to) {
        if (from == null) return true; // Initial status
        
        switch (from) {
            case TODO:
                return to == Status.IN_PROGRESS || to == Status.CANCELLED;
            case IN_PROGRESS:
                return to == Status.REVIEW || to == Status.DONE || to == Status.CANCELLED;
            case REVIEW:
                return to == Status.DONE || to == Status.IN_PROGRESS;
            case DONE:
                return to == Status.IN_PROGRESS; // Reopen task
            case CANCELLED:
                return to == Status.TODO; // Reactivate task
            default:
                return false;
        }
    }
    
    private void updateProgressBasedOnStatus() {
        switch (status) {
            case TODO:
                this.progressPercentage = 0.0;
                break;
            case IN_PROGRESS:
                if (this.progressPercentage == 0.0) {
                    this.progressPercentage = 25.0;
                }
                break;
            case REVIEW:
                this.progressPercentage = 90.0;
                break;
            case DONE:
                this.progressPercentage = 100.0;
                break;
            case CANCELLED:
                // Keep current progress
                break;
        }
    }
    
    // ENUM definitions
    public enum Status {
        TODO("To Do", "#6c757d"),
        IN_PROGRESS("In Progress", "#007bff"),
        REVIEW("Review", "#ffc107"),
        DONE("Done", "#28a745"),
        CANCELLED("Cancelled", "#dc3545");
        
        private final String displayName;
        private final String color;
        
        Status(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
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
        
        public String getDisplayName() { return displayName; }
        public int getLevel() { return level; }
        public String getColor() { return color; }
    }
}
```

### 7.4 Implementation di Project Class

```java
public class Project extends BaseEntity implements Trackable {
    
    private String name;
    private String description;
    private Status status;
    private Priority priority;
    private User creator;
    private LocalDate startDate;
    private LocalDate endDate;
    private double progressPercentage;
    private BigDecimal budget;
    
    // IMPLEMENT Trackable interface
    
    @Override
    public String getCurrentStatus() {
        return status != null ? status.name() : "UNKNOWN";
    }
    
    @Override
    public void updateStatus(String newStatus, User updatedBy) {
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status tidak boleh kosong");
        }
        
        if (updatedBy == null) {
            throw new IllegalArgumentException("UpdatedBy tidak boleh null");
        }
        
        // Check permission
        if (!updatedBy.hasRole(User.Role.ADMIN) && 
            !updatedBy.hasRole(User.Role.MANAGER) &&
            !updatedBy.equals(creator)) {
            throw new SecurityException("Tidak memiliki permission untuk mengubah status project");
        }
        
        Status oldStatus = this.status;
        Status newStatusEnum;
        
        try {
            newStatusEnum = Status.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status tidak valid: " + newStatus);
        }
        
        if (!isValidStatusTransition(oldStatus, newStatusEnum)) {
            throw new IllegalStateException("Transisi status tidak valid dari " + 
                                          oldStatus + " ke " + newStatusEnum);
        }
        
        this.status = newStatusEnum;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy.getId();
        
        // Auto-update progress and dates
        updateProgressBasedOnStatus();
        updateDatesBasedOnStatus();
        
        System.out.println("Project '" + name + "' status changed from " + 
                          oldStatus + " to " + newStatusEnum + " by " + updatedBy.getUsername());
    }
    
    @Override
    public List<StatusHistory> getStatusHistory() {
        // Implementation would fetch from database
        return new ArrayList<>();
    }
    
    @Override
    public LocalDateTime getLastUpdated() {
        return getUpdatedAt();
    }
    
    @Override
    public boolean isUpdatable() {
        return status != Status.COMPLETED && status != Status.CANCELLED;
    }
    
    @Override
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    // HELPER methods
    
    private boolean isValidStatusTransition(Status from, Status to) {
        if (from == null) return true;
        
        switch (from) {
            case PLANNING:
                return to == Status.ACTIVE || to == Status.CANCELLED;
            case ACTIVE:
                return to == Status.ON_HOLD || to == Status.COMPLETED || to == Status.CANCELLED;
            case ON_HOLD:
                return to == Status.ACTIVE || to == Status.CANCELLED;
            case COMPLETED:
                return to == Status.ACTIVE; // Reopen project
            case CANCELLED:
                return to == Status.PLANNING; // Reactivate project
            default:
                return false;
        }
    }
    
    private void updateProgressBasedOnStatus() {
        switch (status) {
            case PLANNING:
                this.progressPercentage = 0.0;
                break;
            case COMPLETED:
                this.progressPercentage = 100.0;
                break;
            case CANCELLED:
                // Keep current progress
                break;
        }
    }
    
    private void updateDatesBasedOnStatus() {
        LocalDate today = LocalDate.now();
        
        switch (status) {
            case ACTIVE:
                if (startDate == null) {
                    startDate = today;
                }
                break;
            case COMPLETED:
                if (endDate == null) {
                    endDate = today;
                }
                break;
        }
    }
    
    // ENUM definitions
    public enum Status {
        PLANNING("Planning", "#6c757d"),
        ACTIVE("Active", "#007bff"),
        ON_HOLD("On Hold", "#ffc107"),
        COMPLETED("Completed", "#28a745"),
        CANCELLED("Cancelled", "#dc3545");
        
        private final String displayName;
        private final String color;
        
        Status(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
}
```

### 7.5 Fungsi Interface dalam Program

#### 1. **Multiple Inheritance**
```java
// Task dapat implement multiple interfaces
public class Task extends BaseEntity implements Assignable, Trackable {
    // Implementation
}

// Polymorphic usage
Assignable assignable = new Task();
Trackable trackable = new Task();
BaseEntity entity = new Task();
```

#### 2. **Contract Definition**
```java
// Interface mendefinisikan kontrak yang harus dipenuhi
public void processAssignables(List<Assignable> items, User manager, User employee) {
    for (Assignable item : items) {
        if (!item.isAssigned()) {
            item.assign(employee, manager);
        }
    }
}

// Method ini dapat menerima semua object yang implement Assignable
List<Assignable> tasks = Arrays.asList(
    new Task("Task 1"),
    new Task("Task 2"),
    new Task("Task 3")
);
processAssignables(tasks, manager, employee);
```

#### 3. **Loose Coupling**
```java
public class NotificationService {
    public void notifyStatusChange(Trackable item, User user) {
        String message = "Status " + item.getClass().getSimpleName() + 
                        " '" + item + "' changed to " + item.getCurrentStatus();
        sendNotification(user, message);
    }
    
    // Method ini tidak perlu tahu apakah item adalah Task atau Project
    // Cukup tahu bahwa item implement Trackable
}
```

---

## 8. Hubungan IS-A dan HAS-A

### 8.1 IS-A Relationships (Inheritance)

| Subclass | Superclass | Relationship | Explanation |
|----------|------------|--------------|-------------|
| User | BaseEntity | User IS-A BaseEntity | User mewarisi properties dan methods dari BaseEntity |
| Project | BaseEntity | Project IS-A BaseEntity | Project mewarisi properties dan methods dari BaseEntity |
| Task | BaseEntity | Task IS-A BaseEntity | Task mewarisi properties dan methods dari BaseEntity |
| UserDAO | BaseDAO<User> | UserDAO IS-A BaseDAO | UserDAO mewarisi template methods dari BaseDAO |
| ProjectDAO | BaseDAO<Project> | ProjectDAO IS-A BaseDAO | ProjectDAO mewarisi template methods dari BaseDAO |
| TaskDAO | BaseDAO<Task> | TaskDAO IS-A BaseDAO | TaskDAO mewarisi template methods dari BaseDAO |
| LoginPanel | JPanel | LoginPanel IS-A JPanel | LoginPanel mewarisi GUI functionality dari JPanel |
| DashboardPanel | JPanel | DashboardPanel IS-A JPanel | DashboardPanel mewarisi GUI functionality dari JPanel |

#### Contoh IS-A Implementation:
```java
// User IS-A BaseEntity
BaseEntity entity = new User(); // Valid - polymorphic assignment
entity.getId(); // Inherited method
entity.getCreatedAt(); // Inherited method
entity.isValid(); // Overridden method

// UserDAO IS-A BaseDAO
BaseDAO<User> dao = new UserDAO(); // Valid - polymorphic assignment
dao.save(user); // Inherited template method
dao.findById(1L); // Inherited method
```

### 8.2 HAS-A Relationships (Composition/Aggregation)

#### Composition (Strong HAS-A)
```java
public class Task extends BaseEntity {
    // Task HAS-A Status (Composition - Status is part of Task)
    private Status status;
    
    // Task HAS-A Priority (Composition - Priority is part of Task)
    private Priority priority;
    
    // Task HAS-A LocalDateTime (Composition - dueDate is part of Task)
    private LocalDateTime dueDate;
    
    // When Task is destroyed, these components are also destroyed
}

public class Project extends BaseEntity {
    // Project HAS-A Status (Composition)
    private Status status;
    
    // Project HAS-A Priority (Composition)
    private Priority priority;
    
    // Project HAS-A LocalDate (Composition)
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Project HAS-A BigDecimal (Composition)
    private BigDecimal budget;
}
```

#### Aggregation (Weak HAS-A)
```java
public class Task extends BaseEntity {
    // Task HAS-A Project (Aggregation - Project exists independently)
    private Project project;
    
    // Task HAS-A User (Aggregation - User exists independently)
    private User assignedUser;
    
    // Task HAS-A User (Aggregation - User exists independently)
    private User assigner;
    
    // When Task is destroyed, Project and Users continue to exist
}

public class Project extends BaseEntity {
    // Project HAS-A User (Aggregation - User exists independently)
    private User creator;
    
    // Project HAS-A List<Task> (Aggregation - Tasks can exist independently)
    private List<Task> tasks;
    
    // When Project is destroyed, creator User continues to exist
    // Tasks may or may not continue to exist (depends on business rules)
}

public class User extends BaseEntity {
    // User HAS-A Role (Aggregation - Role is an enum, exists independently)
    private Role role;
    
    // User HAS-A List<Task> (Aggregation - Tasks exist independently)
    private List<Task> assignedTasks;
    
    // User HAS-A List<Project> (Aggregation - Projects exist independently)
    private List<Project> createdProjects;
}
```

#### GUI HAS-A Relationships
```java
public class TaskManagerApp extends JFrame {
    // TaskManagerApp HAS-A LoginPanel (Composition)
    private LoginPanel loginPanel;
    
    // TaskManagerApp HAS-A DashboardPanel (Composition)
    private DashboardPanel dashboardPanel;
    
    // TaskManagerApp HAS-A User (Aggregation)
    private User currentUser;
    
    // TaskManagerApp HAS-A DatabaseManager (Aggregation)
    private DatabaseManager databaseManager;
}

public class DashboardPanel extends JPanel {
    // DashboardPanel HAS-A ProjectManagementPanel (Composition)
    private ProjectManagementPanel projectPanel;
    
    // DashboardPanel HAS-A TaskManagementPanel (Composition)
    private TaskManagementPanel taskPanel;
    
    // DashboardPanel HAS-A UserManagementPanel (Composition)
    private UserManagementPanel userPanel;
    
    // DashboardPanel HAS-A User (Aggregation)
    private User currentUser;
}
```

### 8.3 Tabel Hubungan Lengkap

| Class A | Relationship | Class B | Type | Explanation |
|---------|--------------|---------|------|-------------|
| User | IS-A | BaseEntity | Inheritance | User extends BaseEntity |
| Task | IS-A | BaseEntity | Inheritance | Task extends BaseEntity |
| Project | IS-A | BaseEntity | Inheritance | Project extends BaseEntity |
| Task | HAS-A | Project | Aggregation | Task belongs to a Project |
| Task | HAS-A | User (assigned) | Aggregation | Task is assigned to a User |
| Task | HAS-A | User (assigner) | Aggregation | Task is assigned by a User |
| Task | HAS-A | Status | Composition | Task has a Status |
| Task | HAS-A | Priority | Composition | Task has a Priority |
| Project | HAS-A | User (creator) | Aggregation | Project is created by a User |
| Project | HAS-A | Status | Composition | Project has a Status |
| Project | HAS-A | Priority | Composition | Project has a Priority |
| User | HAS-A | Role | Composition | User has a Role |
| TaskDAO | IS-A | BaseDAO | Inheritance | TaskDAO extends BaseDAO |
| UserDAO | IS-A | BaseDAO | Inheritance | UserDAO extends BaseDAO |
| ProjectDAO | IS-A | BaseDAO | Inheritance | ProjectDAO extends BaseDAO |
| TaskManagerApp | HAS-A | LoginPanel | Composition | App contains LoginPanel |
| TaskManagerApp | HAS-A | DashboardPanel | Composition | App contains DashboardPanel |
| TaskManagerApp | HAS-A | User | Aggregation | App has current User |
| DashboardPanel | HAS-A | ProjectManagementPanel | Composition | Dashboard contains ProjectPanel |
| DashboardPanel | HAS-A | TaskManagementPanel | Composition | Dashboard contains TaskPanel |
| DashboardPanel | HAS-A | UserManagementPanel | Composition | Dashboard contains UserPanel |

### 8.4 Implementasi dalam Kode

#### IS-A Example
```java
// Polymorphic method yang menerima BaseEntity
public void saveEntity(BaseEntity entity) {
    if (entity.isValid()) {
        entity.updateTimestamps();
        // Save to database
    }
}

// Dapat dipanggil dengan subclass manapun
saveEntity(new User()); // User IS-A BaseEntity
saveEntity(new Project()); // Project IS-A BaseEntity
saveEntity(new Task()); // Task IS-A BaseEntity
```

#### HAS-A Example
```java
public class TaskService {
    public void assignTask(Task task, User employee, User manager) {
        // Task HAS-A User (assigned)
        task.setAssignedUser(employee);
        
        // Task HAS-A User (assigner)
        task.setAssigner(manager);
        
        // Task HAS-A Status
        task.setStatus(Task.Status.TODO);
        
        // Save relationships
        taskDAO.save(task);
    }
    
    public void createProjectTask(Project project, String title, User creator) {
        Task task = new Task();
        task.setTitle(title);
        
        // Task HAS-A Project
        task.setProject(project);
        
        // Task HAS-A User (creator)
        task.setCreatedBy(creator.getId());
        
        taskDAO.save(task);
    }
}
```

---

## 📋 Kesimpulan

Implementasi OOP dalam Task Manager Application mendemonstrasikan:

1. **Enkapsulasi**: Data protection melalui private fields dan controlled access
2. **Inheritance**: Code reuse dan polymorphism melalui BaseEntity dan BaseDAO
3. **Polimorfisme**: Runtime dan compile-time polymorphism untuk flexibility
4. **Abstraksi**: Abstract classes dan interfaces untuk contract definition
5. **Composition/Aggregation**: Proper object relationships dan dependency management

Semua konsep OOP ini bekerja sama untuk menciptakan aplikasi yang maintainable, extensible, dan robust.