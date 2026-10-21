package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import dao.UserDAO;
import dao.impl.UserDAOImpl;
import model.User;

public class PendingRegistrationsPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private UserDAO udao = new UserDAOImpl();

    public PendingRegistrationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // table (cells not editable)
        model = new DefaultTableModel(new String[] { "ID", "Name", "Email", "Role", "Contact" }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // buttons
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        JButton refreshBtn = new JButton("Refresh");

        approveBtn.addActionListener(e -> changeStatus("APPROVED"));
        rejectBtn.addActionListener(e -> changeStatus("REJECTED"));
        refreshBtn.addActionListener(e -> loadPending());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.add(approveBtn);
        btns.add(rejectBtn);
        btns.add(refreshBtn);
        add(btns, BorderLayout.SOUTH);

        loadPending();
    }

    // fill table with pending users
    private void loadPending() {
        model.setRowCount(0);
        try {
            List<User> pending = udao.getPendingUsers();
            for (User u : pending) {
                model.addRow(new Object[] { u.getId(), u.getName(), u.getMail(), u.getRole(), u.getContactNo() });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // approve or reject the selected row
    private void changeStatus(String status) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a registration first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            udao.updateStatus(id, status);
            loadPending();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
