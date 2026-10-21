package ui;

import javax.swing.*;
import java.awt.*;
import model.User;
import service.Library;

public class DashboardFrame extends JFrame {
    private User u;
    private Library lib;

    public DashboardFrame(User u, Library lib) {
        super("Library System");
        this.u = u;
        this.lib = lib;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // check if user is admin
        boolean isadmin = "ADMIN".equalsIgnoreCase(u.getRole());

        // top bar with welcome msg and logout
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // show role or borrowing limit
        String info = "";
        if (isadmin) {
            info = " | Admin";
        } else {
            info = " | Limit: " + u.getMaxBookAllowed() + " books";
        }

        JLabel lbl = new JLabel("Welcome, " + u.getName() + " (" + u.getRole() + ")" + info);
        JButton btn = new JButton("Logout");

        // back to login
        btn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        top.add(lbl, BorderLayout.WEST);
        top.add(btn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // tabs for different sections
        JTabbedPane tp = new JTabbedPane();
        tp.addTab("Search Books", new BookSearchPanel(lib));

        if (isadmin) {
            // admin tabs
            tp.addTab("Manage Books", new ManageBooksPanel(lib));
            tp.addTab("Reports", new ReportPanel(lib));
        } else {
            // borrower tabs
            tp.addTab("Borrow / Return", new IssueReturnPanel(u, lib, lib));
            tp.addTab("My Loans", new LoansPanel(u));
        }

        add(tp, BorderLayout.CENTER);

        setSize(800, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // for testing directly
    public static void main(String[] args) {
        model.User tu = new model.Student("Test Student", "student@test.com", 1, "1234567890", "pass123");
        new DashboardFrame(tu, new Library());
    }
}
