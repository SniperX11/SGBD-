package com.sgbd.ui;

import com.sgbd.model.Department;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window.
 *
 * Layout: a horizontal {@link JSplitPane} with the
 * {@link DepartmentPanel} on the left and the {@link EmployeePanel} on the right.
 * Selecting a department row immediately refreshes the employee list.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("SGBD Lab 1 – Department / Employee Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 600);
        setMinimumSize(new Dimension(800, 450));
        setLocationRelativeTo(null); // centre on screen

        // ---- Build child panels ----
        DepartmentPanel departmentPanel = new DepartmentPanel();
        EmployeePanel   employeePanel   = new EmployeePanel();

        // ---- Wire selection callback ----
        departmentPanel.setDepartmentSelectionListener(
                (Department dept) -> employeePanel.loadEmployeesForDepartment(dept));

        // ---- Split pane ----
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, departmentPanel, employeePanel);
        splitPane.setDividerLocation(320);

        // ---- Status bar ----
        JLabel statusBar = new JLabel(
                " Select a department to view its employees.");
        statusBar.setBorder(BorderFactory.createEtchedBorder());

        // ---- Assemble ----
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }
}
