package com.sgbd.ui;

import com.sgbd.dao.DepartmentDAO;
import com.sgbd.model.Department;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel displaying the parent (Department) table.
 *
 * When the user selects a row, all registered {@link DepartmentSelectionListener}s
 * are notified so that the child panel can refresh its employee list.
 */
public class DepartmentPanel extends JPanel {

    // -----------------------------------------------------------------------
    // Selection-change callback interface
    // -----------------------------------------------------------------------

    /** Listener notified when the selected department changes. */
    public interface DepartmentSelectionListener {
        void onDepartmentSelected(Department department);
    }

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private DepartmentSelectionListener selectionListener;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;

    /** Currently loaded departments (same order as table rows). */
    private List<Department> currentDepartments;

    // Column indices in the table model
    private static final int COL_ID       = 0;
    private static final int COL_NAME     = 1;
    private static final int COL_LOCATION = 2;
    private static final int COL_BUDGET   = 3;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public DepartmentPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Departments (Parent)"));

        // ---- Search bar ----
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh");
        searchPanel.add(refreshBtn, BorderLayout.EAST);
        add(searchPanel, BorderLayout.NORTH);

        // ---- Table ----
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Location", "Budget"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only parent table
            }
            @Override
            public Class<?> getColumnClass(int col) {
                return col == COL_ID ? Integer.class :
                       col == COL_BUDGET ? Double.class : String.class;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true); // bonus: sortable columns
        table.getColumnModel().getColumn(COL_ID).setMaxWidth(60);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- Wire events ----
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    notifySelectionChanged();
                }
            }
        });

        // Live search as user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { loadDepartments(); }
            @Override public void removeUpdate(DocumentEvent e)  { loadDepartments(); }
            @Override public void changedUpdate(DocumentEvent e) { loadDepartments(); }
        });

        refreshBtn.addActionListener(e -> loadDepartments());

        // Initial load
        loadDepartments();
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Registers the listener that will be notified on row selection. */
    public void setDepartmentSelectionListener(DepartmentSelectionListener listener) {
        this.selectionListener = listener;
    }

    /** Reloads department data from the database and refreshes the table. */
    public void loadDepartments() {
        try {
            String filter = searchField.getText().trim();
            if (filter.isEmpty()) {
                currentDepartments = departmentDAO.findAll();
            } else {
                currentDepartments = departmentDAO.findByNameFilter(filter);
            }
            populateTable(currentDepartments);
        } catch (SQLException ex) {
            showError("Failed to load departments: " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void populateTable(List<Department> departments) {
        tableModel.setRowCount(0);
        for (Department d : departments) {
            tableModel.addRow(new Object[]{d.getId(), d.getName(),
                                           d.getLocation(), d.getBudget()});
        }
    }

    private void notifySelectionChanged() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || selectionListener == null) return;

        // Convert view index to model index (important when sorting is active)
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= 0 && modelRow < currentDepartments.size()) {
            selectionListener.onDepartmentSelected(currentDepartments.get(modelRow));
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
