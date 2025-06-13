# Task Manager Application

Sebuah aplikasi manajemen tugas berbasis Java Swing yang dirancang untuk penggunaan internal perusahaan/organisasi. Aplikasi ini memungkinkan administrator dan manajer untuk mengelola proyek, menugaskan tugas kepada karyawan, dan melacak progress pekerjaan.

## 📋 Deskripsi Program

Task Manager adalah aplikasi desktop yang dibangun menggunakan Java Swing dengan arsitektur MVC (Model-View-Controller). Aplikasi ini menerapkan konsep Object-Oriented Programming (OOP) secara komprehensif dan menggunakan MySQL sebagai database.

### Fitur Utama:
- **Manajemen User**: Registrasi, login, dan pengelolaan pengguna dengan role-based access
- **Manajemen Proyek**: Membuat, mengedit, dan melacak status proyek
- **Manajemen Tugas**: Menugaskan, memperbarui status, dan monitoring tugas
- **Dashboard**: Overview statistik dan aktivitas terbaru
- **Role-based Access Control**: Admin, Manager, dan Employee dengan hak akses berbeda

## 🏗️ Arsitektur dan Desain Class

### Struktur Package:
```
src/
├── Main.java                 # Entry point aplikasi
├── models/                   # Model classes (Entity)
│   ├── BaseEntity.java      # Abstract base class
│   ├── User.java            # User entity
│   ├── Project.java         # Project entity
│   └── Task.java            # Task entity
├── interfaces/               # Interface definitions
│   ├── Assignable.java      # Interface untuk assignment
│   └── Trackable.java       # Interface untuk tracking
├── dao/                      # Data Access Objects
│   ├── BaseDAO.java         # Abstract DAO class
│   ├── UserDAO.java         # User data operations
│   ├── ProjectDAO.java      # Project data operations
│   └── TaskDAO.java         # Task data operations
├── gui/                      # GUI components
│   ├── TaskManagerApp.java  # Main application window
│   ├── LoginPanel.java      # Login interface
│   ├── DashboardPanel.java  # Main dashboard
│   ├── ProjectManagementPanel.java
│   ├── TaskManagementPanel.java
│   ├── UserManagementPanel.java
│   └── [Dialog classes]
└── database/                 # Database management
    └── DatabaseManager.java # Database connection & setup
```

### Class Diagram (UML):
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   BaseEntity    │    │   Assignable    │    │   Trackable     │
│   (Abstract)    │    │   (Interface)   │    │   (Interface)   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ - id: Long      │    │ + assign()      │    │ + updateStatus()│
│ - createdAt     │    │ + unassign()    │    │ + getHistory()  │
│ - updatedAt     │    │ + isAssigned()  │    │ + isUpdatable() │
│ - createdBy     │    └─────────────────┘    └─────────────────┘
│ - updatedBy     │              ▲                      ▲
└─────────────────┘              │                      │
         ▲                       │                      │
         │                       │                      │
    ┌────┴────┐              ┌───┴───┐              ┌───┴───┐
    │  User   │              │ Task  │              │Project│
    │         │              │       │              │       │
    │         │              │       │              │       │
    └─────────┘              └───────┘              └───────┘
```

## 🔧 Implementasi OOP

### 1. Enkapsulasi

**Private Fields dengan Getter/Setter:**
```java
public class User extends BaseEntity {
    private String username;        // Private field
    private String passwordHash;    // Private field
    private String email;          // Private field
    
    // Public getter
    public String getUsername() {
        return username;
    }
    
    // Public setter dengan validasi
    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        this.username = username.trim();
    }
}
```

**Protected Fields untuk Inheritance:**
```java
public abstract class BaseEntity {
    protected Long id;              // Protected untuk subclass
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
```

### 2. Inheritance (Pewarisan)

**Hubungan IS-A:**
| Superclass | Subclass | Relationship |
|------------|----------|-------------|
| BaseEntity | User | User IS-A BaseEntity |
| BaseEntity | Project | Project IS-A BaseEntity |
| BaseEntity | Task | Task IS-A BaseEntity |
| BaseDAO<T> | UserDAO | UserDAO IS-A BaseDAO |
| BaseDAO<T> | ProjectDAO | ProjectDAO IS-A BaseDAO |
| BaseDAO<T> | TaskDAO | TaskDAO IS-A BaseDAO |

**Method Override Example:**
```java
// Di BaseEntity
public abstract class BaseEntity {
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
}

// Di User (Override)
public class User extends BaseEntity {
    @Override
    public String toString() {
        return "User[id=" + getId() + ", username=" + username + "]";
    }
}
```

**HAS-A Relationships:**
- Task HAS-A User (assigned user)
- Task HAS-A Project
- Project HAS-A User (creator)
- Task HAS-A User (assigner)

### 3. Tingkat Akses

| Modifier | Usage | Alasan |
|----------|-------|--------|
| **private** | Fields, helper methods | Enkapsulasi data, hide implementation |
| **protected** | BaseEntity fields | Akses untuk subclass inheritance |
| **public** | API methods, constructors | Interface untuk client code |
| **default** | Package-private utilities | Akses dalam package yang sama |

### 4. Polimorfisme

**Referensi Polimorfik:**
```java
// Referensi superclass, objek subclass
BaseEntity entity = new User();
BaseEntity entity2 = new Project();
BaseEntity entity3 = new Task();

// Method overriding
System.out.println(entity.toString());  // Calls User.toString()
System.out.println(entity2.toString()); // Calls Project.toString()
```

**Method Overloading:**
```java
public class TaskDAO extends BaseDAO<Task> {
    // Overloaded methods
    public List<Task> findByStatus(Task.Status status) { ... }
    public List<Task> findByStatus(String status) { ... }
    public List<Task> findByStatus(Task.Status status, int limit) { ... }
}
```

**Interface Implementation:**
```java
// Polimorfisme melalui interface
Assignable assignableTask = new Task();
Trackable trackableProject = new Project();

// Runtime polymorphism
assignableTask.assign(user);  // Calls Task.assign()
trackableProject.updateStatus(status); // Calls Project.updateStatus()
```

### 5. Abstract Class dan Method

**BaseEntity (Abstract Class):**
```java
public abstract class BaseEntity {
    // Concrete methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    // Abstract method - must be implemented by subclasses
    public abstract boolean isValid();
}
```

**BaseDAO (Abstract Class):**
```java
public abstract class BaseDAO<T extends BaseEntity> {
    // Template method pattern
    public boolean save(T entity) {
        if (entity.getId() == null) {
            return insert(entity);  // Calls abstract method
        } else {
            return update(entity);  // Calls abstract method
        }
    }
    
    // Abstract methods - implemented by subclasses
    protected abstract String getTableName();
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
}
```

**Alasan Penggunaan Abstract:**
- **Code Reuse**: Menghindari duplikasi kode common functionality
- **Template Method Pattern**: Mendefinisikan algoritma umum, detail implementasi di subclass
- **Enforce Contract**: Memaksa subclass mengimplementasikan method tertentu

### 6. Interface

**Assignable Interface:**
```java
public interface Assignable {
    void assign(User user, User assigner);
    void unassign();
    User getAssignedUser();
    User getAssigner();
    boolean isAssigned();
}
```

**Trackable Interface:**
```java
public interface Trackable {
    String getCurrentStatus();
    void updateStatus(String newStatus, User updatedBy);
    List<String> getStatusHistory();
    LocalDateTime getLastUpdated();
    boolean isUpdatable();
}
```

**Implementation di Task:**
```java
public class Task extends BaseEntity implements Assignable, Trackable {
    @Override
    public void assign(User user, User assigner) {
        this.assignedUser = user;
        this.assigner = assigner;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public void updateStatus(String newStatus, User updatedBy) {
        this.status = Task.Status.valueOf(newStatus);
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
```

**Fungsi Interface:**
- **Multiple Inheritance**: Java tidak support multiple class inheritance, tapi bisa implement multiple interfaces
- **Contract Definition**: Mendefinisikan kontrak yang harus dipenuhi implementing class
- **Loose Coupling**: Mengurangi ketergantungan antar class
- **Polymorphism**: Memungkinkan polimorfisme melalui interface reference

## 🗄️ Desain dan Implementasi Database

### Entity Relationship Diagram:
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     USERS       │    │    PROJECTS     │    │     TASKS       │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ id (PK)         │    │ id (PK)         │    │ id (PK)         │
│ username        │    │ name            │    │ title           │
│ password_hash   │    │ description     │    │ description     │
│ email           │    │ status          │    │ status          │
│ full_name       │    │ creator_id (FK) │    │ priority        │
│ role            │    │ created_at      │    │ project_id (FK) │
│ created_at      │    │ updated_at      │    │ assigned_user_id│
│ updated_at      │    │ created_by      │    │ assigner_id (FK)│
│ created_by      │    │ updated_by      │    │ due_date        │
│ updated_by      │    └─────────────────┘    │ created_at      │
└─────────────────┘                          │ updated_at      │
         │                                   │ created_by      │
         │                                   │ updated_by      │
         └───────────────┐                   └─────────────────┘
                         │                            │
                         ▼                            │
              ┌─────────────────┐                     │
              │ STATUS_HISTORY  │                     │
              ├─────────────────┤                     │
              │ id (PK)         │                     │
              │ entity_type     │◄────────────────────┘
              │ entity_id       │
              │ old_status      │
              │ new_status      │
              │ changed_by (FK) │
              │ changed_at      │
              │ notes           │
              └─────────────────┘
```

### Koneksi Database (JDBC):
```java
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/task_manager";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
```

### Query Examples:
```java
// UserDAO - Find by username
public User findByUsername(String username) throws SQLException {
    String sql = "SELECT * FROM users WHERE username = ?";
    try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? mapResultSet(rs) : null;
    }
}

// TaskDAO - Find by assigned user
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
    // Implementation...
}
```

### Fungsi Setiap Tabel:

| Tabel | Fungsi |
|-------|--------|
| **users** | Menyimpan data pengguna, authentication, dan role |
| **projects** | Menyimpan informasi proyek dan hubungan dengan creator |
| **tasks** | Menyimpan tugas, assignment, dan hubungan dengan project |
| **status_history** | Audit trail untuk perubahan status project/task |

## 🚀 Cara Menjalankan

### Prerequisites:
1. **Java Development Kit (JDK) 17+**
2. **MySQL Server** (XAMPP recommended)
3. **MySQL Connector/J** (JDBC Driver)

### Setup Database:
1. Start XAMPP MySQL service
2. Database akan dibuat otomatis saat aplikasi pertama kali dijalankan
3. Default admin user:
   - Username: `admin`
   - Password: `admin123`

### Compile dan Run:
```bash
# Compile
javac -cp ".:mysql-connector-java-8.0.33.jar" src/**/*.java

# Run
java -cp ".:mysql-connector-java-8.0.33.jar:src" Main
```

### Atau menggunakan IDE:
1. Import project ke IDE (IntelliJ IDEA, Eclipse, NetBeans)
2. Add MySQL Connector/J ke classpath
3. Run `Main.java`

## 👥 User Roles

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access: manage users, projects, tasks |
| **MANAGER** | Manage projects and tasks, assign tasks |
| **EMPLOYEE** | View assigned tasks, update task status |

## 🎨 UI/UX Design

### Color Scheme:
- **Primary**: #2C3E50 (Dark Blue-Gray)
- **Secondary**: #ECF0F1 (Light Gray)
- **Success**: #27AE60 (Green)
- **Warning**: #F39C12 (Orange)
- **Danger**: #E74C3C (Red)
- **Info**: #3498DB (Blue)

### Design Principles:
- **Clean & Modern**: Minimalist design dengan fokus pada functionality
- **Professional**: Color scheme yang cocok untuk lingkungan kerja
- **Responsive**: Layout yang adaptif dengan berbagai ukuran window
- **Intuitive**: Navigation yang mudah dipahami

## 📝 Fitur Tambahan

- **Search & Filter**: Pencarian dan filter data di semua tabel
- **Status Tracking**: History perubahan status project dan task
- **Role-based UI**: Interface menyesuaikan dengan role user
- **Data Validation**: Validasi input di semua form
- **Error Handling**: Penanganan error yang user-friendly

## 🔧 Teknologi yang Digunakan

- **Java 17+**: Programming language
- **Swing**: GUI framework
- **MySQL**: Database management system
- **JDBC**: Database connectivity
- **BCrypt**: Password hashing
- **Maven/Gradle**: Build tool (optional)

## 📄 License

Project ini dibuat untuk keperluan akademik (UAS PBO).

---

**Developed by**: [Your Name]  
**Course**: Pemrograman Berorientasi Objek  
**Institution**: [Your Institution]  
**Year**: 2024