package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import model.*;
import service.Library;

public class ManageBooksPanel extends JPanel {
    private Library l;
    private DefaultTableModel model;
    private JTable t;
    private JTextField srch;
    private JButton sbtn, allbtn, addbtn, delbtn;

    // fill the table with items
    private void filtable(List<LibraryItem> lst) {
        model.setRowCount(0);
        for (LibraryItem it : lst) {
            model.addRow(new Object[] {
                it.getIsbn(),
                it.getTitle(),
                it.getAuthor(),
                it.getType(),
                it.getAvailableCopies(),
                it.getTotalCopies(),
                it.getShelfLocation()
            });
        }
    }

    // search query
    private void search() {
        String txt = srch.getText().trim();
        filtable(l.search(txt));
    }

    // dialog to insert book
    private void addb() {
        JTextField isb = new JTextField();
        JTextField titl = new JTextField();
        JTextField auth = new JTextField();
        JComboBox<String> tpbox = new JComboBox<>(new String[] { "TEXT BOOK", "REFERENCE BOOK", "JOURNAL" });
        JTextField cps = new JTextField("1");
        JTextField shlf = new JTextField("A1");

        // text inside popup
        JPanel pan = new JPanel(new GridLayout(6, 2, 6, 6));
        pan.add(new JLabel("ISBN:"));
        pan.add(isb);
        pan.add(new JLabel("Title:"));
        pan.add(titl);
        pan.add(new JLabel("Author:"));
        pan.add(auth);
        pan.add(new JLabel("Type:"));
        pan.add(tpbox);
        pan.add(new JLabel("Copies:"));
        pan.add(cps);
        pan.add(new JLabel("Shelf:"));
        pan.add(shlf);

        int opt = JOptionPane.showConfirmDialog(this, pan, "Add Book", JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) {
            return;
        }

        String code = isb.getText().trim();
        String name = titl.getText().trim();
        String writer = auth.getText().trim();
        String kind = (String) tpbox.getSelectedItem();
        String loc = shlf.getText().trim();

        if (code.isEmpty() || name.isEmpty() || writer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
            return;
        }

        int count = 1;
        try {
            count = Integer.parseInt(cps.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid number of copies.");
            return;
        }

        LibraryItem item;
        if ("REFERENCE BOOK".equalsIgnoreCase(kind)) {
            item = new ReferenceBook(code, name, writer, count, count, loc);
        } else if ("JOURNAL".equalsIgnoreCase(kind)) {
            item = new Journal(code, name, writer, count, count, loc);
        } else {
            item = new TextBook(code, name, writer, count, count, loc);
        }

        l.addItem(item);
        search();
        JOptionPane.showMessageDialog(this, "Book added.");
    }

    // delete book
    private void delb() {
        int r = t.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Pick a book first.");
            return;
        }

        String isb_val = (String) model.getValueAt(r, 0);
        try {
            l.removeItem(isb_val);
            search();
            JOptionPane.showMessageDialog(this, "Book removed.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public ManageBooksPanel(Library l) {
        this.l = l;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // search controls
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        srch = new JTextField(14);
        sbtn = new JButton("Search");
        allbtn = new JButton("Show All");

        sbtn.addActionListener(e -> search());
        srch.addActionListener(e -> search());
        allbtn.addActionListener(e -> {
            srch.setText("");
            search();
        });

        bar.add(new JLabel("Search:"));
        bar.add(srch);
        bar.add(sbtn);
        bar.add(allbtn);
        add(bar, BorderLayout.NORTH);

        // table setup
        String[] headers = { "ISBN", "Title", "Author", "Type", "Available", "Total", "Shelf" };
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        t = new JTable(model);
        add(new JScrollPane(t), BorderLayout.CENTER);

        // bottom action buttons
        addbtn = new JButton("Add Book");
        delbtn = new JButton("Remove Selected");

        addbtn.addActionListener(e -> addb());
        delbtn.addActionListener(e -> delb());

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.add(addbtn);
        bot.add(delbtn);
        add(bot, BorderLayout.SOUTH);

        search();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Manage Books");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new ManageBooksPanel(new Library()));
        f.pack();
        f.setSize(700, 420);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
