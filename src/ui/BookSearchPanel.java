package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import service.Library;

public class BookSearchPanel extends JPanel {
    private JTextField searchtxt;
    private JButton searchbtn;
    private JButton showbtn;
    private DefaultTableModel tablem;
    private JTable bookt;
    private JScrollPane sp;
    private JLabel statuslbl;
    private Library lib;

    // load books into the table rows
    private void poptable(java.util.List<model.LibraryItem> items) {
        tablem.setRowCount(0);
        for (model.LibraryItem item : items) {
            tablem.addRow(new Object[] { item.getIsbn(), item.getTitle(), item.getAuthor(),
                    item.getAvailableCopies(), item.getTotalCopies(), item.getShelfLocation() });
        }
        statuslbl.setText("Found: " + items.size() + " books.");
    }

    // search books by title or isbn
    private void dosearchex() {
        String query = searchtxt.getText().trim();
        java.util.List<model.LibraryItem> results = lib.search(query);
        poptable(results);
    }

    public BookSearchPanel() {
        this(new Library());
    }

    public BookSearchPanel(Library lib) {
        this.lib = lib;
        String[] cols = { "ISBN", "Title", "Author", "Available", "Total", "Shelf Location" };

        tablem = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        bookt = new JTable(tablem);
        sp = new JScrollPane(bookt);

        searchtxt = new JTextField(12);
        searchbtn = new JButton("Search");
        showbtn = new JButton("Show All");
        statuslbl = new JLabel("Loading books...");

        // search on button click or enter key
        searchbtn.addActionListener(e -> dosearchex());
        searchtxt.addActionListener(e -> dosearchex());
        showbtn.addActionListener(e -> {
            searchtxt.setText("");
            dosearchex();
        });

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top search bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(searchtxt);
        top.add(searchbtn);
        top.add(showbtn);

        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(statuslbl, BorderLayout.SOUTH);

        // load all books on start
        dosearchex();
    }

    // test frame
    public static void main(String[] args) {
        JFrame f = new JFrame("Books");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new BookSearchPanel());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
