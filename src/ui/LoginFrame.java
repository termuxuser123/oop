package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField emailtxt;
    private JPasswordField passtxt;
    private JButton loginbtn;
    private JButton regbtn;

    public LoginFrame() {
        super("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // fields for input
        emailtxt = new JTextField(10);
        passtxt = new JPasswordField(10);
        loginbtn = new JButton("Login");
        regbtn = new JButton("Register");

        // open registration
        regbtn.addActionListener(e -> new RegisterFrame());

        // handle login click
        loginbtn.addActionListener(e -> {
            String email = emailtxt.getText();
            String pass = new String(passtxt.getPassword());

            // check empty fields
            if (email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password.");
                return;
            }

            // authenticate with database
            try {
                dao.UserDAO udao = new dao.impl.UserDAOImpl();
                model.User u = udao.authenticate(email, pass);
                if (u != null) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    new DashboardFrame(u, new service.Library());
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // simple grid layout
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        p.add(new JLabel("Email:"));
        p.add(emailtxt);
        p.add(new JLabel("Password:"));
        p.add(passtxt);
        p.add(loginbtn);
        p.add(regbtn);

        add(p);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        LoginFrame w = new LoginFrame();
        w.setVisible(true);
    }
}