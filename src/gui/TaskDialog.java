package gui;

import dao.*;
import models.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for creating and editing tasks
 * Demonstrates form handling, validation, and task management
 */
public class TaskDialog extends JDialog {
    
    private Task task;
    private User currentUser;
    private ProjectDAO projectDAO;
    private TaskDAO taskDAO;
    private UserDAO userDAO;
    private boolean confirmed = false;
    
    // GUI Components
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Task.Status> statusCombo;
    private JComboBox<Task.Priority> priorityCombo;
    private JComboBox<Project> projectCombo;
    private JComboBox<User> assigneeCombo;
    private JTextField dueDateField;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    /**
     * Constructor for creating/editing tasks
     * @param parent Parent window
     * @param title Dialog title
     * @param task Task to edit (null for new task)
     * @param currentUser Currently logged in user
     * @param projectDAO Project data access object
     * @param userDAO User data access object
     */
    public TaskDialog(Window parent, String title, Task task, User currentUser,
                     ProjectDAO projectDAO, UserDAO userDAO) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        
        this.task = task;
        this.currentUser = currentUser;
        this.projectDAO = projectDAO;
        this.taskDAO = new TaskDAO();
        this.userDAO = userDAO;
        
        setupDialog();
        createComponents();
        layoutComponents();
        addEventListeners();
        loadData();
        
        if (task != null) {
            populateFields();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    /**
     * Sets up the dialog properties
     */
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(TaskManagerApp.BACKGROUND_COLOR);
    }
    
    /**
     * Creates all GUI components
     */
    private void createComponents() {
        // Create input fields
        titleField = TaskManagerApp.createStyledTextField();
        titleField.setPreferredSize(new Dimension(300, 35));
        
        descriptionArea = new JTextArea(4, 25);
        descriptionArea.setFont(TaskManagerApp.BODY_FONT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TaskManagerApp.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        // Create combo boxes
        statusCombo = new JComboBox<>(Task.Status.values());
        statusCombo.setFont(TaskManagerApp.BODY_FONT);
        statusCombo.setPreferredSize(new Dimension(150, 35));
        
        priorityCombo = new JComboBox<>(Task.Priority.values());
        priorityCombo.setFont(TaskManagerApp.BODY_FONT);
        priorityCombo.setPreferredSize(new Dimension(150, 35));
        
        projectCombo = new JComboBox<>();
        projectCombo.setFont(TaskManagerApp.BODY_FONT);
        projectCombo.setPreferredSize(new Dimension(300, 35));
        
        assigneeCombo = new JComboBox<>();
        assigneeCombo.setFont(TaskManagerApp.BODY_FONT);
        assigneeCombo.setPreferredSize(new Dimension(300, 35));
        
        dueDateField = TaskManagerApp.createStyledTextField();
        dueDateField.setPreferredSize(new Dimension(150, 35));
        dueDateField.setToolTipText("Format: YYYY-MM-DD (e.g., 2024-12-31)");
        
        // Create buttons
        saveButton = TaskManagerApp.createStyledButton(
            task == null ? "Create Task" : "Update Task",
            TaskManagerApp.SUCCESS_COLOR,
            Color.WHITE
        );
        
        cancelButton = TaskManagerApp.createStyledButton(
            "Cancel",
            TaskManagerApp.SECONDARY_COLOR,
            TaskManagerApp.TEXT_PRIMARY
        );
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.DANGER_COLOR);
    }
    
    /**
     * Layouts all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title section
        JPanel titlePanel = createFieldPanel("Title *:", titleField);
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Description section
        JPanel descPanel = new JPanel(new BorderLayout(10, 5));
        descPanel.setBackground(Color.WHITE);
        
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(TaskManagerApp.BODY_FONT);
        descLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        descPanel.add(descLabel, BorderLayout.NORTH);
        descPanel.add(descScrollPane, BorderLayout.CENTER);
        
        mainPanel.add(descPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Status and Priority section
        JPanel statusPriorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPriorityPanel.setBackground(Color.WHITE);
        
        JPanel statusPanel = createFieldPanel("Status:", statusCombo);
        JPanel priorityPanel = createFieldPanel("Priority:", priorityCombo);
        
        statusPriorityPanel.add(statusPanel);
        statusPriorityPanel.add(Box.createHorizontalStrut(20));
        statusPriorityPanel.add(priorityPanel);
        
        mainPanel.add(statusPriorityPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Project section
        JPanel projectPanel = createFieldPanel("Project:", projectCombo);
        mainPanel.add(projectPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Assignee section (only for admin/manager)
        if (canAssignTasks()) {
            JPanel assigneePanel = createFieldPanel("Assign To:", assigneeCombo);
            mainPanel.add(assigneePanel);
            mainPanel.add(Box.createVerticalStrut(15));
        }
        
        // Due Date section
        JPanel dueDatePanel = createFieldPanel("Due Date:", dueDateField);
        mainPanel.add(dueDatePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Status label
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates a field panel with label and component
     * @param labelText Label text
     * @param component Component
     * @return Field panel
     */
    private JPanel createFieldPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(TaskManagerApp.BODY_FONT);
        label.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        saveButton.addActionListener(e -> saveTask());
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        // Enter key on title field
        titleField.addActionListener(e -> saveTask());
    }
    
    /**
     * Loads data for combo boxes
     */
    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadProjects();
                if (canAssignTasks()) {
                    loadUsers();
                }
                return null;
            }
            
            @Override
            protected void done() {
                // Data loaded
            }
        };
        
        worker.execute();
    }
    
    /**
     * Loads projects for the project combo box
     */
    private void loadProjects() {
        try {
            projectCombo.addItem(null); // No project option
            
            List<Project> projects;
            if (currentUser.getRole() == User.Role.ADMIN) {
                projects = projectDAO.findAll();
            } else {
                // Managers and employees see projects they created or are involved in
                projects = projectDAO.findByCreatedBy(currentUser.getId());
            }
            
            for (Project project : projects) {
                projectCombo.addItem(project);
            }
            
            // Custom renderer to show project names
            projectCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    
                    if (value == null) {
                        setText("No Project");
                    } else if (value instanceof Project) {
                        Project project = (Project) value;
                        setText(project.getName());
                    }
                    
                    return this;
                }
            });
            
        } catch (Exception e) {
            showStatusMessage("Failed to load projects: " + e.getMessage());
        }
    }
    
    /**
     * Loads users for the assignee combo box
     */
    private void loadUsers() {
        try {
            assigneeCombo.addItem(null); // Unassigned option
            
            List<User> users = userDAO.findAll();
            for (User user : users) {
                assigneeCombo.addItem(user);
            }
            
            // Custom renderer to show user names
            assigneeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    
                    if (value == null) {
                        setText("Unassigned");
                    } else if (value instanceof User) {
                        User user = (User) value;
                        setText(user.getFullName() + " (" + user.getUsername() + ")");
                    }
                    
                    return this;
                }
            });
            
        } catch (Exception e) {
            showStatusMessage("Failed to load users: " + e.getMessage());
        }
    }
    
    /**
     * Populates fields with existing task data
     */
    private void populateFields() {
        if (task == null) return;
        
        titleField.setText(task.getTitle());
        descriptionArea.setText(task.getDescription());
        statusCombo.setSelectedItem(task.getStatus());
        priorityCombo.setSelectedItem(task.getPriority());
        
        // Set project
        if (task.getProject() != null) {
            for (int i = 0; i < projectCombo.getItemCount(); i++) {
                Project project = projectCombo.getItemAt(i);
                if (project != null && project.getId() == task.getProject().getId()) {
                    projectCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        // Set assignee (only for admin/manager)
        if (canAssignTasks() && task.getAssignedUser() != null) {
            for (int i = 0; i < assigneeCombo.getItemCount(); i++) {
                User user = assigneeCombo.getItemAt(i);
                if (user != null && user.getId() == task.getAssignedUser().getId()) {
                    assigneeCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        // Set due date
        if (task.getDueDate() != null) {
            dueDateField.setText(task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }
    
    /**
     * Saves the task
     */
    private void saveTask() {
        if (!validateForm()) {
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                if (task == null) {
                    // Create new task
                    task = new Task();
                }
                
                // Set task properties
                task.setTitle(titleField.getText().trim());
                task.setDescription(descriptionArea.getText().trim());
                task.setStatus((Task.Status) statusCombo.getSelectedItem());
                task.setPriority((Task.Priority) priorityCombo.getSelectedItem());
                task.setProject((Project) projectCombo.getSelectedItem());
                
                // Set assignee (only for admin/manager)
                if (canAssignTasks()) {
                    User selectedUser = (User) assigneeCombo.getSelectedItem();
                    if (selectedUser != null) {
                        task.assignTo(selectedUser, currentUser);
                    }
                }
                
                // Set due date
                String dueDateText = dueDateField.getText().trim();
                if (!dueDateText.isEmpty()) {
                    try {
                        LocalDate dueDate = LocalDate.parse(dueDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        task.setDueDate(dueDate);
                    } catch (DateTimeParseException e) {
                        throw new Exception("Invalid due date format. Use YYYY-MM-DD");
                    }
                } else {
                    task.setDueDate(null);
                }
                
                // Save to database
                return taskDAO.save(task);
            }
            
            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        confirmed = true;
                        showStatusMessage("Task saved successfully!");
                        
                        // Close dialog after 1 second
                        Timer timer = new Timer(1000, e -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatusMessage("Failed to save task to database");
                    }
                } catch (Exception e) {
                    showStatusMessage("Failed to save task: " + e.getMessage());
                    e.printStackTrace(); // For debugging
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Validates the form data
     * @return True if form is valid
     */
    private boolean validateForm() {
        // Clear previous status
        clearStatusMessage();
        
        // Validate title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showStatusMessage("Title is required");
            titleField.requestFocus();
            return false;
        }
        
        if (title.length() > 200) {
            showStatusMessage("Title must be 200 characters or less");
            titleField.requestFocus();
            return false;
        }
        
        // Validate description length
        String description = descriptionArea.getText().trim();
        if (description.length() > 1000) {
            showStatusMessage("Description must be 1000 characters or less");
            descriptionArea.requestFocus();
            return false;
        }
        
        // Validate due date format
        String dueDateText = dueDateField.getText().trim();
        if (!dueDateText.isEmpty()) {
            try {
                LocalDate dueDate = LocalDate.parse(dueDateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // Check if due date is in the past
                if (dueDate.isBefore(LocalDate.now())) {
                    showStatusMessage("Due date cannot be in the past");
                    dueDateField.requestFocus();
                    return false;
                }
            } catch (DateTimeParseException e) {
                showStatusMessage("Invalid due date format. Use YYYY-MM-DD (e.g., 2024-12-31)");
                dueDateField.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if current user can assign tasks
     * @return True if user can assign tasks
     */
    private boolean canAssignTasks() {
        return currentUser.getRole() == User.Role.ADMIN || currentUser.getRole() == User.Role.MANAGER;
    }
    
    /**
     * Shows status message
     * @param message Status message
     */
    private void showStatusMessage(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(TaskManagerApp.DANGER_COLOR);
    }
    
    /**
     * Clears status message
     */
    private void clearStatusMessage() {
        statusLabel.setText(" ");
    }
    
    /**
     * Gets the task
     * @return Task object
     */
    public Task getTask() {
        return task;
    }
    
    /**
     * Checks if dialog was confirmed
     * @return True if confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}