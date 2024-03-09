/*
 * $Id: JTable2Pdf.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.objects.tables.alternatives;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;

/**
 * Constructing a JTable and printing it to PDF.
 */
public class JTable2Pdf extends JFrame {

    private static final long serialVersionUID = 8461166420041411734L;
    /**
     * The JTable we will show in a Swing app and print to PDF.
     */
    private JTable table;

    /**
     * Constructor for PrintJTable.
     */
    public JTable2Pdf() {
        getContentPane().setLayout(new BorderLayout());
        setTitle("JTable test");
        createToolbar();
        createTable();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * A very simple PdfPTable example.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Printing a JTable");
        JTable2Pdf frame = new JTable2Pdf();
        frame.pack();
        frame.setVisible(true);
        frame.print();
        frame.dispose();
    }

    /**
     * Create a table with some dummy data
     */
    private void createTable() {
        Object[][] data = {
                {"Mary", "Campione", "Snowboarding", 5, Boolean.FALSE},
                {"Alison", "Huml", "Rowing", 3, Boolean.TRUE},
                {"Kathy", "Walrath", "Chasing toddlers",
                        2, Boolean.FALSE},
                {"Mark", "Andrews", "Speed reading", 20, Boolean.TRUE},
                {"Angela", "Lih", "Teaching high school", 4, Boolean.FALSE}
        };

        String[] columnNames =
                {"First Name", "Last Name", "Sport", "# of Years", "Vegetarian"};

        table = new JTable(data, columnNames);

        // Use a panel to contains the table and add it the frame
        JPanel tPanel = new JPanel(new BorderLayout());
        tPanel.add(table.getTableHeader(), BorderLayout.NORTH);
        tPanel.add(table, BorderLayout.CENTER);

        getContentPane().add(tPanel, BorderLayout.CENTER);
    }

    /**
     * Toolbar for print and exit
     */
    private void createToolbar() {
        JToolBar tb = new JToolBar();

        JButton printBtn = new JButton("Print");
        printBtn.addActionListener(e -> print());

        JButton exitBtn = new JButton("Exit");
        exitBtn.addActionListener(e -> exit());

        tb.add(printBtn);
        tb.add(exitBtn);

        getContentPane().add(tb, BorderLayout.NORTH);
    }

    /**
     * Print the table into a PDF file
     */
    private void print() {
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter writer =
                    PdfWriter.getInstance(document, new FileOutputStream("jTable.pdf"));

            document.open();
            PdfContentByte cb = writer.getDirectContent();

            // Create the graphics as shapes
            cb.saveState();
            Graphics2D g2 = cb.createGraphicsShapes(500, 500);
            // Print the table to the graphics
            Shape oldClip = g2.getClip();
            g2.clipRect(0, 0, 500, 500);
            table.print(g2);
            g2.setClip(oldClip);

            g2.dispose();
            cb.restoreState();

            document.newPage();

            // Create the graphics with pdf fonts
            cb.saveState();
            g2 = cb.createGraphics(500, 500);

            // Print the table to the graphics
            oldClip = g2.getClip();
            g2.clipRect(0, 0, 500, 500);
            table.print(g2);
            g2.setClip(oldClip);

            g2.dispose();
            cb.restoreState();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        document.close();
    }

    /**
     * Exit app
     */
    private void exit() {
        System.exit(0);
    }
}