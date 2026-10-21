package ui;

import java.awt.*;
import javax.swing.*;

public class IssueReturnPanel extends JPanel {
    private JComboBox<String> bookbox;
    private JButton issuebtn;
    private JButton returnbtn;
    private JLabel statuslbl;
    private model.User user;
    private service.Borrowable borrowable;
    private service.Searchable searchable;

    // fill dropdown with books and copy count
    private void loadb() {
        bookbox.removeAllItems();
        java.util.List<model.LibraryItem> allbooks = searchable.search("");
        for (model.LibraryItem b : allbooks) {
            bookbox.addItem(b.getIsbn() + " - " + b.getTitle() + " (" + b.getAvailableCopies() + " left)");
        }
    }

    // get isbn from dropdown text
    private String getsisbn() {
        String sel = (String) bookbox.getSelectedItem();
        if (sel == null || sel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a book.");
            return null;
        }
        return sel.split(" - ")[0].trim();
    }

    // find the item by isbn
    private model.LibraryItem findbookbisbn(String isbn) {
        if (isbn == null || isbn.isEmpty()) {
            return null;
        }

        java.util.List<model.LibraryItem> res = searchable.search(isbn);
        for (model.LibraryItem it : res) {
            if (it.getIsbn().equalsIgnoreCase(isbn)) {
                return it;
            }
        }
        JOptionPane.showMessageDialog(this, "Book not found with ISBN: " + isbn);
        return null;
    }

    // capture console print messages
    private String runwo(Runnable act) {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        java.io.PrintStream orig = System.out;
        try {
            System.setOut(new java.io.PrintStream(buf));
            act.run();
        } finally {
            System.setOut(orig);
        }
        String out = buf.toString().trim();
        System.out.println(out);
        return out;
    }

    public IssueReturnPanel(model.User user, service.Borrowable borrowable, service.Searchable searchable) {
        this.user = user;
        this.borrowable = borrowable;
        this.searchable = searchable;

        bookbox = new JComboBox<>();
        loadb();

        issuebtn = new JButton("Borrow/ Issue Book");
        returnbtn = new JButton("Return Book");
        statuslbl = new JLabel("Select a book and click Borrow or Return.");

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Circulation Desk (Borrow / Return)"),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // grid form for the dropdown and buttons
        JPanel formp = new JPanel(new GridLayout(2, 2, 10, 10));
        formp.add(new JLabel("Select Book:"));
        formp.add(bookbox);
        formp.add(issuebtn);
        formp.add(returnbtn);
        add(formp, BorderLayout.NORTH);
        add(statuslbl, BorderLayout.SOUTH);

        // issue button click
        issuebtn.addActionListener(e -> {
            String isbn = getsisbn();
            model.LibraryItem it = findbookbisbn(isbn);
            if (it != null) {
                try {
                    String res = runwo(() -> borrowable.issue(user, it));
                    if (res.isEmpty()) {
                        res = "Book issued successfully: " + it.getTitle();
                    }
                    statuslbl.setText(res);
                    loadb();

                    int msgtype = JOptionPane.INFORMATION_MESSAGE;
                    if (res.contains("ERROR")) {
                        msgtype = JOptionPane.WARNING_MESSAGE;
                    }
                    JOptionPane.showMessageDialog(this, res, "Issue Status", msgtype);
                } catch (Exception ex) {
                    statuslbl.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // return button click
        returnbtn.addActionListener(e -> {
            String isbn = getsisbn();
            model.LibraryItem it = findbookbisbn(isbn);
            if (it != null) {
                try {
                    String res = runwo(() -> borrowable.returnItem(user, it));
                    if (res.isEmpty()) {
                        res = "Returned successfully: " + it.getTitle();
                    }
                    statuslbl.setText(res);
                    loadb();

                    int msgtype = JOptionPane.INFORMATION_MESSAGE;
                    if (res.contains("ERROR")) {
                        msgtype = JOptionPane.WARNING_MESSAGE;
                    }
                    JOptionPane.showMessageDialog(this, res, "Return Status", msgtype);
                } catch (Exception ex) {
                    statuslbl.setText("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
