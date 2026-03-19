package com.sgbd.dao;

import com.sgbd.db.DatabaseManager;
import com.sgbd.model.Employee;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code employees} table.
 *
 * All SQL queries use {@link PreparedStatement} to prevent SQL injection.
 * Every method obtains its own connection and closes it in a
 * try-with-resources block to guarantee proper resource management.
 */
public class EmployeeDAO {

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    /**
     * Returns all employees belonging to the given department.
     *
     * @param departmentId the parent department id
     */
    public List<Employee> findByDepartment(int departmentId) throws SQLException {
        String sql = "SELECT id, name, email, salary, hire_date, department_id " +
                     "FROM employees " +
                     "WHERE department_id = ? " +
                     "ORDER BY name";
        List<Employee> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Returns employees in a department whose name or email contains {@code filter}.
     */
    public List<Employee> findByDepartmentAndFilter(int departmentId,
                                                    String filter) throws SQLException {
        String sql = "SELECT id, name, email, salary, hire_date, department_id " +
                     "FROM employees " +
                     "WHERE department_id = ? " +
                     "  AND (LOWER(name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)) " +
                     "ORDER BY name";
        List<Employee> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeParam = "%" + filter + "%";
            ps.setInt(1, departmentId);
            ps.setString(2, likeParam);
            ps.setString(3, likeParam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    /**
     * Inserts a new employee and returns the generated primary key.
     *
     * @param employee employee with all fields populated except {@code id}
     * @return the newly assigned id
     */
    public int insert(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (name, email, salary, hire_date, department_id) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getEmail());
            ps.setDouble(3, employee.getSalary());
            ps.setDate(4, Date.valueOf(employee.getHireDate()));
            ps.setInt(5, employee.getDepartmentId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("INSERT did not return a generated key.");
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    /**
     * Updates an existing employee record.
     *
     * @param employee employee with updated fields; {@code id} must be valid
     */
    public void update(Employee employee) throws SQLException {
        String sql = "UPDATE employees " +
                     "SET name = ?, email = ?, salary = ?, hire_date = ?, department_id = ? " +
                     "WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, employee.getName());
            ps.setString(2, employee.getEmail());
            ps.setDouble(3, employee.getSalary());
            ps.setDate(4, Date.valueOf(employee.getHireDate()));
            ps.setInt(5, employee.getDepartmentId());
            ps.setInt(6, employee.getId());
            ps.executeUpdate();
        }
    }

    // -----------------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------------

    /**
     * Deletes the employee with the given id.
     *
     * @param id primary key of the employee to delete
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /** Maps the current ResultSet row to an {@link Employee} object. */
    private Employee mapRow(ResultSet rs) throws SQLException {
        LocalDate hireDate = rs.getDate("hire_date") != null
                ? rs.getDate("hire_date").toLocalDate()
                : LocalDate.now();
        return new Employee(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getDouble("salary"),
                hireDate,
                rs.getInt("department_id")
        );
    }
}
