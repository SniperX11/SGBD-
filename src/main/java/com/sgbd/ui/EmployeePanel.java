package com.sgbd.ui;

import com.sgbd.dao.EmployeeDAO;
import com.sgbd.model.Department;
import com.sgbd.model.Employee;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Panel displaying the child (Employee) table for the currently selected
 * department, together with an edit form and CRUD buttons.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────────┐
 * │  Search bar                                   [Ref] │
 * ├────────────────────────────┬────────────────────────┤
 * │  Employee table (scroll)   │  Edit form             │
 * │                            │  Name: ___             │
 * │                            │  Email: ___            │
 * │                            │  Salary: ___           │
 * │                            │  Hire Date: ___        │
 * │                            │  [Add] [Save] [Delete] │
 * └────────────────────────────┴────────────────────────┘
 */
public class EmployeePanel extends JPanel {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    private Department currentDepartment;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    // Edit form fields
    private final JTextField nameField     = new JTextField();
    private final JTextField emailField    = new JTextField();
    private final JTextField salaryField   = new JTextField();
    private final JTextField hireDateField = new JTextField();

    // Buttons
    private final JButton addBtn    = new JButton("Add");
    private final JButton saveBtn   = new JButton("Save");
    private final JButton deleteBtn = new JButton("Delete");
    private final JButton clearBtn  = new JButton("Clear Form");

    /** Id of the employee currently being edited (−1 = new / add mode). */
    private int editingEmployeeId = -1;

    /** Currently loaded employees (same order as table rows). */
    private List<Employee> currentEmployees;

    // Column indices
    private static final int COL_ID        = 0;
    private static final int COL_NAME      = 1;
    private static final int COL_EMAIL     = 2;
    private static final int COL_SALARY    = 3;
    private static final int COL_HIRE_DATE = 4;

    // Simple email-format validator
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public EmployeePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Employees (Child)"));

        // ---- Search / status bar ----
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchField = new JTextField();
        searchField.setToolTipText("Filter employees by name or email");
        topPanel.add(searchField, BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh");
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ---- Table ----
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Email", "Salary", "Hire Date"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int c) {
                return c == COL_ID ? Integer.class :
                       c == COL_SALARY ? Double.class : String.class;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // bonus: sortable columns
        table.getColumnModel().getColumn(COL_ID).setMaxWidth(60);

        // ---- Edit form ----
        JPanel formPanel = buildFormPanel();

        // ---- Split pane: table left, form right ----
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(table), formPanel);
        splitPane.setResizeWeight(0.65);
        add(splitPane, BorderLayout.CENTER);

        // ---- Wire events ----
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { loadEmployees(); }
            @Override public void removeUpdate(DocumentEvent e)  { loadEmployees(); }
            @Override public void changedUpdate(DocumentEvent e) { loadEmployees(); }
        });

        refreshBtn.addActionListener(e -> loadEmployees());
        addBtn.addActionListener(e -> handleAdd());
        saveBtn.addActionListener(e -> handleSave());
        deleteBtn.addActionListener(e -> handleDelete());
        clearBtn.addActionListener(e -> clearForm());

        setDepartmentSelected(false);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Called by {@link DepartmentPanel} when the user selects a department row.
     * Loads the employees for that department and clears the form.
     */
    public void loadEmployeesForDepartment(Department department) {
        this.currentDepartment = department;
        clearForm();
        loadEmployees();
        setDepartmentSelected(true);
    }

    // -----------------------------------------------------------------------
    // Form building
    // -----------------------------------------------------------------------

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        // Fields grid
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(fields, gbc, 0, "Name *:", nameField,
                "Full name of the employee");
        addFormRow(fields, gbc, 1, "Email *:", emailField,
                "Valid email address, must be unique");
        addFormRow(fields, gbc, 2, "Salary *:", salaryField,
                "Monthly gross salary (must be > 0)");
        addFormRow(fields, gbc, 3, "Hire Date *:", hireDateField,
                "Format: YYYY-MM-DD");

        panel.add(fields, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        btnPanel.add(addBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addFormRow(JPanel parent, GridBagConstraints gbc,
                            int row, String labelText,
                            JTextField field, String tooltip) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        parent.add(new JLabel(labelText), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        field.setToolTipText(tooltip);
        parent.add(field, gbc);
    }

    // -----------------------------------------------------------------------
    // Load / display
    // -----------------------------------------------------------------------

    private void loadEmployees() {
        if (currentDepartment == null) return;
        try {
            String filter = searchField.getText().trim();
            if (filter.isEmpty()) {
                currentEmployees = employeeDAO.findByDepartment(
                        currentDepartment.getId());
            } else {
                currentEmployees = employeeDAO.findByDepartmentAndFilter(
                        currentDepartment.getId(), filter);
            }
            populateTable(currentEmployees);
        } catch (SQLException ex) {
            showError("Failed to load employees: " + ex.getMessage());
        }
    }

    private void populateTable(List<Employee> employees) {
        tableModel.setRowCount(0);
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                emp.getId(),
                emp.getName(),
                emp.getEmail(),
                emp.getSalary(),
                emp.getHireDate() != null ? emp.getHireDate().toString() : ""
            });
        }
    }

    private void populateFormFromSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            clearForm();
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (currentEmployees == null || modelRow >= currentEmployees.size()) return;

        Employee emp = currentEmployees.get(modelRow);
        editingEmployeeId = emp.getId();

        nameField.setText(emp.getName());
        emailField.setText(emp.getEmail());
        salaryField.setText(String.valueOf(emp.getSalary()));
        hireDateField.setText(emp.getHireDate() != null
                ? emp.getHireDate().toString() : "");

        saveBtn.setEnabled(true);
        deleteBtn.setEnabled(true);
        addBtn.setEnabled(false);
    }

    // -----------------------------------------------------------------------
    // CRUD handlers
    // -----------------------------------------------------------------------

    private void handleAdd() {
        Employee emp = buildEmployeeFromForm();
        if (emp == null) return; // validation failed

        try {
            employeeDAO.insert(emp);
            loadEmployees();
            clearForm();
            JOptionPane.showMessageDialog(this,
                    "Employee added successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Failed to add employee: " + friendlyMessage(ex));
        }
    }

    private void handleSave() {
        if (editingEmployeeId < 0) return;

        Employee emp = buildEmployeeFromForm();
        if (emp == null) return;
        emp.setId(editingEmployeeId);

        try {
            employeeDAO.update(emp);
            loadEmployees();
            clearForm();
            JOptionPane.showMessageDialog(this,
                    "Employee updated successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Failed to update employee: " + friendlyMessage(ex));
        }
    }

    private void handleDelete() {
        if (editingEmployeeId < 0) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this employee?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            employeeDAO.delete(editingEmployeeId);
            loadEmployees();
            clearForm();
            JOptionPane.showMessageDialog(this,
                    "Employee deleted.", "Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Failed to delete employee: " + friendlyMessage(ex));
        }
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    /**
     * Reads and validates the form fields.
     *
     * @return a populated {@link Employee} if validation passes, {@code null} otherwise
     */
    private Employee buildEmployeeFromForm() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String salaryText = salaryField.getText().trim();
        String hireDateText = hireDateField.getText().trim();

        // Required-field check
        if (name.isEmpty() || email.isEmpty() ||
                salaryText.isEmpty() || hireDateText.isEmpty()) {
            showError("All fields marked with * are required.");
            return null;
        }

        // Email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            return null;
        }

        // Salary: must be a positive number
        double salary;
        try {
            salary = Double.parseDouble(salaryText);
            if (salary <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Salary must be a positive number.");
            return null;
        }

        // Hire date: must be a valid ISO date
        LocalDate hireDate;
        try {
            hireDate = LocalDate.parse(hireDateText);
        } catch (DateTimeParseException e) {
            showError("Hire Date must be in YYYY-MM-DD format.");
            return null;
        }

        Employee emp = new Employee();
        emp.setName(name);
        emp.setEmail(email);
        emp.setSalary(salary);
        emp.setHireDate(hireDate);
        emp.setDepartmentId(currentDepartment.getId());
        return emp;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void clearForm() {
        editingEmployeeId = -1;
        nameField.setText("");
        emailField.setText("");
        salaryField.setText("");
        hireDateField.setText("");
        table.clearSelection();
        addBtn.setEnabled(currentDepartment != null);
        saveBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
    }

    private void setDepartmentSelected(boolean selected) {
        addBtn.setEnabled(selected);
        saveBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        if (!selected) {
            tableModel.setRowCount(0);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Converts a raw {@link SQLException} into a user-friendly message.
     * Detects common constraint violations.
     */
    private String friendlyMessage(SQLException ex) {
        String msg = ex.getMessage();
        if (msg == null) return "Unknown database error.";
        if (msg.contains("unique") || msg.contains("duplicate")) {
            return "An employee with this email already exists.";
        }
        if (msg.contains("foreign key") || msg.contains("violates")) {
            return "Operation violates a database constraint.";
        }
        if (msg.contains("check")) {
            return "Salary must be greater than 0.";
        }
        return msg;
    }
}
