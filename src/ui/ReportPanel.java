package ui;

import javax.swing.*;
import java.awt.*;
import service.Reportable;

public class ReportPanel extends JPanel {
    private Reportable rep;
    private JTextArea repoarea;
    private JButton refbtn;

    // fetch summary from library
    private void refreport() {
        if (rep != null) {
            repoarea.setText(rep.generateSummary());
        }
    }

    public ReportPanel(Reportable rep) {
        this.rep = rep;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // text area to show summary output
        repoarea = new JTextArea();
        repoarea.setEditable(false);
        JScrollPane sp = new JScrollPane(repoarea);

        // top bar with refresh button
        JPanel top = new JPanel(new BorderLayout());
        JLabel titlelbl = new JLabel("System Reports & Statistics");
        titlelbl.setFont(titlelbl.getFont().deriveFont(Font.BOLD, 14f));
        refbtn = new JButton("Refresh Report");
        refbtn.addActionListener(e -> refreport());

        top.add(titlelbl, BorderLayout.WEST);
        top.add(refbtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        // initial load
        refreport();
    }

    // test method
    public static void main(String[] args) {
        JFrame f = new JFrame("Test Reports");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new ReportPanel(new service.Library()));
        f.pack();
        f.setSize(500, 350);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
