package gui;

import dao.*;
import models.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User management panel for viewing and managing users (Admin only)
 * Demonstrates role-based access control and user administration
 */
public class UserManagementPanel extends JPanel {
    
    private User currentUser;
    private UserDAO userDAO;
    
    // GUI Components
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton createButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton resetPasswordButton;
    private JTextField searchField;
    private JComboBox<String> roleFilter;
    private JLabel statusLabel;
    
    // Pagination components
    private JButton firstPageButton;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JButton lastPageButton;
    private JLabel pageInfoLabel;
    
    // Pagination state
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalItems = 0;
    private List<User> allUsers;
    
    // Table columns
    private final String[] columnNames = {
        "ID", "Username", "Full Name", "Email", "Role", "Created", "Updated"
    };
    
    /**
     * Constructor initializes the user management panel
     * @param currentUser Currently logged in user
     * @param userDAO User data access object
     */
    public UserManagementPanel(User currentUser, UserDAO userDAO) {
        this.currentUser = currentUser;
        this.userDAO = userDAO;
        
        // Only allow admin access
        if (currentUser.getRole() != User.Role.ADMIN) {
            setupAccessDeniedPanel();
            return;
        }
        
        setupPanel();
        createComponents();
        layoutComponents();
        addEventListeners();
        loadUsers();
    }
    
    /**
     * Sets up access denied panel for non-admin users
     */
    private void setupAccessDeniedPanel() {
        setLayout(new BorderLayout());
        setBackground(TaskManagerApp.BACKGROUND_COLOR);
        
        JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JLabel messageLabel = new JLabel("<html><center>" +
            "<h2>Access Denied</h2>" +
            "<p>You don't have permission to access user management.</p>" +
            "<p>Only administrators can manage users.</p>" +
            "</center></html>");
        messageLabel.setFont(TaskManagerApp.BODY_FONT);
        messageLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        messagePanel.add(messageLabel);
        add(messagePanel, BorderLayout.CENTER);
    }
    
    /**
     * Sets up the panel properties
     */
    private void setupPanel() {
        setLayout(new BorderLayout());
        setBackground(TaskManagerApp.BACKGROUND_COLOR);
    }
    
    /**
     * Creates all GUI components
     */
    private void createComponents() {
        // Create table
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setFont(TaskManagerApp.BODY_FONT);
        userTable.setRowHeight(40);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setGridColor(TaskManagerApp.BORDER_COLOR);
        userTable.setShowGrid(true);
        
        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Username
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Full Name
        userTable.getColumnModel().getColumn(3).setPreferredWidth(250); // Email
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Role
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Created
        userTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Updated
        
        // Custom cell renderer for role column
        userTable.getColumnModel().getColumn(4).setCellRenderer(new RoleCellRenderer());
        
        // Create buttons
        createButton = TaskManagerApp.createStyledButton(
            "Create User", 
            TaskManagerApp.SUCCESS_COLOR, 
            Color.WHITE
        );
        
        editButton = TaskManagerApp.createStyledButton(
            "Edit User", 
            TaskManagerApp.WARNING_COLOR, 
            Color.WHITE
        );
        editButton.setEnabled(false);
        
        deleteButton = TaskManagerApp.createStyledButton(
            "Delete User", 
            TaskManagerApp.DANGER_COLOR, 
            Color.WHITE
        );
        deleteButton.setEnabled(false);
        
        resetPasswordButton = TaskManagerApp.createStyledButton(
            "Reset Password", 
            TaskManagerApp.INFO_COLOR, 
            Color.WHITE
        );
        resetPasswordButton.setEnabled(false);
        
        // Create search field
        searchField = TaskManagerApp.createStyledTextField();
        searchField.setPreferredSize(new Dimension(200, 35));
        
        // Create role filter
        String[] roleOptions = {"All Roles", "ADMIN", "MANAGER", "EMPLOYEE"};
        roleFilter = new JComboBox<>(roleOptions);
        roleFilter.setFont(TaskManagerApp.BODY_FONT);
        roleFilter.setPreferredSize(new Dimension(120, 35));
        
        // Create status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(TaskManagerApp.SMALL_FONT);
        statusLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        
        // Create pagination buttons
        firstPageButton = TaskManagerApp.createStyledButton("<<", TaskManagerApp.PRIMARY_COLOR, Color.WHITE);
        prevPageButton = TaskManagerApp.createStyledButton("<", TaskManagerApp.PRIMARY_COLOR, Color.WHITE);
        nextPageButton = TaskManagerApp.createStyledButton(">", TaskManagerApp.PRIMARY_COLOR, Color.WHITE);
        lastPageButton = TaskManagerApp.createStyledButton(">>", TaskManagerApp.PRIMARY_COLOR, Color.WHITE);
        
        firstPageButton.setPreferredSize(new Dimension(80, 40));
        prevPageButton.setPreferredSize(new Dimension(80, 40));
        nextPageButton.setPreferredSize(new Dimension(80, 40));
        lastPageButton.setPreferredSize(new Dimension(80, 40));
        
        pageInfoLabel = new JLabel("Page 1 of 1");
        pageInfoLabel.setFont(TaskManagerApp.SMALL_FONT);
        pageInfoLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
        pageInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pageInfoLabel.setPreferredSize(new Dimension(100, 30));
    }
    
    /**
     * Layouts all components
     */
    private void layoutComponents() {
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(TaskManagerApp.TITLE_FONT);
        titleLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(statusLabel, BorderLayout.EAST);
        
        // Toolbar panel
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbarPanel.setBackground(Color.WHITE);
        toolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TaskManagerApp.BORDER_COLOR));
        
        // Search section
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(TaskManagerApp.BODY_FONT);
        searchLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        // Filter section
        JLabel roleFilterLabel = new JLabel("Role:");
        roleFilterLabel.setFont(TaskManagerApp.BODY_FONT);
        roleFilterLabel.setForeground(TaskManagerApp.TEXT_PRIMARY);
        
        toolbarPanel.add(searchLabel);
        toolbarPanel.add(searchField);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(roleFilterLabel);
        toolbarPanel.add(roleFilter);
        toolbarPanel.add(Box.createHorizontalStrut(20));
        toolbarPanel.add(createButton);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(resetPasswordButton);
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        // Pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        paginationPanel.setBackground(Color.WHITE);
        paginationPanel.add(firstPageButton);
        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageInfoLabel);
        paginationPanel.add(nextPageButton);
        paginationPanel.add(lastPageButton);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(paginationPanel, BorderLayout.SOUTH);
        
        // Main layout
        add(titlePanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Adds event listeners to components
     */
    private void addEventListeners() {
        // Table selection listener
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = userTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                resetPasswordButton.setEnabled(hasSelection);
                
                // Don't allow deleting current user
                if (hasSelection) {
                    Integer selectedUserId = (Integer) tableModel.getValueAt(userTable.getSelectedRow(), 0);
                    deleteButton.setEnabled(!selectedUserId.equals(currentUser.getId()));
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });
        
        // Table double-click listener
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && userTable.getSelectedRow() != -1) {
                    editUser();
                }
            }
        });
        
        // Button listeners
        createButton.addActionListener(e -> createUser());
        editButton.addActionListener(e -> editUser());
        deleteButton.addActionListener(e -> deleteUser());
        resetPasswordButton.addActionListener(e -> resetPassword());
        
        // Search and filter listeners
        searchField.addActionListener(e -> {
            currentPage = 1;
            filterUsers();
        });
        roleFilter.addActionListener(e -> {
            currentPage = 1;
            filterUsers();
        });
        
        // Pagination listeners
        firstPageButton.addActionListener(e -> {
            currentPage = 1;
            updateTableWithPagination();
        });
        
        prevPageButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateTableWithPagination();
            }
        });
        
        nextPageButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            if (currentPage < totalPages) {
                currentPage++;
                updateTableWithPagination();
            }
        });
        
        lastPageButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            currentPage = Math.max(1, totalPages);
            updateTableWithPagination();
        });
    }
    
    /**
     * Loads users into the table
     */
    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userDAO.findAll();
            }
            
            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    updateTable(users);
                    updateStatusLabel(users.size() + " users loaded");
                } catch (Exception e) {
                    showErrorMessage("Failed to load users: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Updates the table with user data
     * @param users List of users
     */
    private void updateTable(List<User> users) {
        this.allUsers = users;
        this.totalItems = users.size();
        this.currentPage = 1;
        updateTableWithPagination();
    }
    
    /**
     * Updates the table with paginated user data
     */
    private void updateTableWithPagination() {
        tableModel.setRowCount(0);
        
        if (allUsers == null || allUsers.isEmpty()) {
            updatePaginationInfo();
            updatePaginationButtons();
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allUsers.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            User user = allUsers.get(i);
            Object[] rowData = {
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt().format(formatter),
                user.getUpdatedAt().format(formatter)
            };
            
            tableModel.addRow(rowData);
        }
        
        updatePaginationInfo();
        updatePaginationButtons();
    }
    
    /**
     * Updates the pagination information label
     */
    private void updatePaginationInfo() {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        pageInfoLabel.setText(String.format("Page %d of %d (%d items)", currentPage, totalPages, totalItems));
    }
    
    /**
     * Updates the state of pagination buttons
     */
    private void updatePaginationButtons() {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / itemsPerPage));
        
        firstPageButton.setEnabled(currentPage > 1);
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < totalPages);
        lastPageButton.setEnabled(currentPage < totalPages);
    }
    
    /**
     * Filters users based on search and filter criteria
     */
    private void filterUsers() {
        String searchText = searchField.getText().trim().toLowerCase();
        String roleText = (String) roleFilter.getSelectedItem();
        
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                List<User> users = userDAO.findAll();
                
                return users.stream()
                    .filter(user -> {
                        // Search filter
                        if (!searchText.isEmpty()) {
                            return user.getUsername().toLowerCase().contains(searchText) ||
                                   user.getFullName().toLowerCase().contains(searchText) ||
                                   user.getEmail().toLowerCase().contains(searchText);
                        }
                        return true;
                    })
                    .filter(user -> {
                        // Role filter
                        if (!"All Roles".equals(roleText)) {
                            return user.getRole().toString().equals(roleText);
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            }
            
            @Override
            protected void done() {
                try {
                    List<User> filteredUsers = get();
                    updateTable(filteredUsers);
                    updateStatusLabel(filteredUsers.size() + " users found");
                } catch (Exception e) {
                    showErrorMessage("Failed to filter users: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Creates a new user
     */
    private void createUser() {
        UserRegistrationDialog dialog = new UserRegistrationDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), userDAO);
        dialog.setVisible(true);
        
        if (dialog.isRegistrationSuccessful()) {
            updateStatusLabel("User created successfully");
            loadUsers();
        }
    }
    
    /**
     * Edits the selected user
     */
    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.findById(userId);
            }
            
            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        UserEditDialog dialog = new UserEditDialog(
                            SwingUtilities.getWindowAncestor(UserManagementPanel.this), 
                            user, userDAO);
                        dialog.setVisible(true);
                        
                        if (dialog.isUpdateSuccessful()) {
                            updateStatusLabel("User updated successfully");
                            loadUsers();
                        }
                    }
                } catch (Exception e) {
                    showErrorMessage("Failed to load user: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Deletes the selected user
     */
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        // Don't allow deleting current user
        if (userId == currentUser.getId()) {
            showErrorMessage("You cannot delete your own account");
            return;
        }
        
        // Create custom icon to prevent clipping
        ImageIcon icon = null;
        try {
            ImageIcon originalIcon = (ImageIcon) UIManager.getIcon("OptionPane.warningIcon");
            if (originalIcon != null) {
                // Scale the icon to a smaller size to prevent clipping
                Image img = originalIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        } catch (Exception e) {
            // Fallback to no icon if there's an issue
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete user '" + username + "'?\n" +
            "This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
             icon
         );
        
        if (result == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return userDAO.deleteById(userId.intValue());
                }
                
                @Override
                protected void done() {
                    try {
                        Boolean success = get();
                        if (success) {
                            updateStatusLabel("User deleted successfully");
                            loadUsers();
                        } else {
                            showErrorMessage("Failed to delete user");
                        }
                    } catch (Exception e) {
                        showErrorMessage("Failed to delete user: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Resets password for the selected user
     */
    private void resetPassword() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        String newPassword = JOptionPane.showInputDialog(
            this,
            "Enter new password for user '" + username + "':",
            "Reset Password",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                showErrorMessage("Password must be at least 6 characters long");
                return;
            }
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return userDAO.updatePassword(userId, newPassword);
                }
                
                @Override
                protected void done() {
                    try {
                        Boolean success = get();
                        if (success) {
                            updateStatusLabel("Password reset successfully");
                            JOptionPane.showMessageDialog(
                                UserManagementPanel.this,
                                "Password for user '" + username + "' has been reset successfully.",
                                "Password Reset",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            showErrorMessage("Failed to reset password");
                        }
                    } catch (Exception e) {
                        showErrorMessage("Failed to reset password: " + e.getMessage());
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    /**
     * Updates the status label
     * @param message Status message
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(TaskManagerApp.TEXT_SECONDARY);
    }
    
    /**
     * Shows error message
     * @param message Error message
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Custom cell renderer for role column
     */
    private class RoleCellRenderer extends JLabel implements TableCellRenderer {
        
        public RoleCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(TaskManagerApp.SMALL_FONT.deriveFont(Font.BOLD));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setText(value.toString());
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(Color.WHITE);
                
                // Set color based on role
                if (value instanceof User.Role) {
                    User.Role role = (User.Role) value;
                    switch (role) {
                        case ADMIN:
                            setForeground(TaskManagerApp.DANGER_COLOR);
                            break;
                        case MANAGER:
                            setForeground(TaskManagerApp.WARNING_COLOR);
                            break;
                        case EMPLOYEE:
                            setForeground(TaskManagerApp.INFO_COLOR);
                            break;
                        default:
                            setForeground(TaskManagerApp.TEXT_PRIMARY);
                    }
                } else {
                    setForeground(TaskManagerApp.TEXT_PRIMARY);
                }
            }
            
            return this;
        }
    }
}