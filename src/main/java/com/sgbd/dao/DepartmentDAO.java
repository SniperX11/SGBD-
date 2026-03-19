package com.sgbd.dao;

import com.sgbd.db.DatabaseManager;
import com.sgbd.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the {@code departments} table.
 * All SQL queries use {@link PreparedStatement} to prevent SQL injection.
 */
public class DepartmentDAO {

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    /**
     * Returns all departments ordered by name.
     */
    public List<Department> findAll() throws SQLException {
        String sql = "SELECT id, name, location, budget FROM departments ORDER BY name";
        List<Department> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Returns all departments whose name contains {@code filter} (case-insensitive).
     */
    public List<Department> findByNameFilter(String filter) throws SQLException {
        String sql = "SELECT id, name, location, budget " +
                     "FROM departments " +
                     "WHERE LOWER(name) LIKE LOWER(?) " +
                     "ORDER BY name";
        List<Department> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + filter + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /** Maps the current ResultSet row to a {@link Department} object. */
    private Department mapRow(ResultSet rs) throws SQLException {
        return new Department(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("location"),
                rs.getDouble("budget")
        );
    }
}
