package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import dao.UserDAO;
import dao.impl.UserDAOImpl;
import model.Admin;
import model.Faculty;
import model.Student;
import model.User;

public class RegisterFrame extends JFrame {
    private JTextField nametxt;
    private JTextField emailtxt;
    private JPasswordField passtxt;
    private JTextField phonetxt;
    private JSpinner agespin;
    private JComboBox<String> rolebox;
    private JButton regbtn;

    public RegisterFrame() {
        super("Register");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // form fields
        nametxt = new JTextField(12);
        emailtxt = new JTextField(12);
        passtxt = new JPasswordField(12);
        phonetxt = new JTextField(12);
        agespin = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));

        String[] roles = { "Student", "Faculty", "Admin" };
        rolebox = new JComboBox<>(roles);
        regbtn = new JButton("Register");

        // submit registration
        regbtn.addActionListener(r -> {
            String name = nametxt.getText().trim();
            String email = emailtxt.getText().trim();
            String pass = new String(passtxt.getPassword());
            String contact = phonetxt.getText().trim();
            String role = (String) rolebox.getSelectedItem();

            // check required fields
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please do not leave any of the required fields blank.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // check email format
            if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                JOptionPane.showMessageDialog(this, "Invalid email address format! (Example: user@example.com)",
                        "Invalid Email", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // check phone digits
            if (!contact.matches("\\d{7,15}")) {
                JOptionPane.showMessageDialog(this, "Contact number must be between 7 and 15 digits.",
                        "Invalid Contact Number", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // create model instance based on role
            try {
                User u;
                if ("Admin".equalsIgnoreCase(role)) {
                    u = new Admin(name, email, 0, contact, pass);
                } else if ("Faculty".equalsIgnoreCase(role)) {
                    u = new Faculty(name, email, 0, contact, pass);
                } else {
                    u = new Student(name, email, 0, contact, pass);
                }

                UserDAO udao = new UserDAOImpl();
                udao.insert(u);
                JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
                    JOptionPane.showMessageDialog(this, "An account with this email already exists.",
                            "Registration Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // layout form in 2 columns
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        p.add(new JLabel("Name:"));
        p.add(nametxt);

        p.add(new JLabel("Email:"));
        p.add(emailtxt);

        p.add(new JLabel("Password:"));
        p.add(passtxt);

        p.add(new JLabel("Contact Number:"));
        p.add(phonetxt);

        p.add(new JLabel("Age:"));
        p.add(agespin);

        p.add(new JLabel("Role:"));
        p.add(rolebox);

        p.add(new JLabel(""));
        p.add(regbtn);

        add(p);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
