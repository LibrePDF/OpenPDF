/*
 * $Id: OldTable.java 3373 2008-05-12 16:21:24Z xlv $
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
package org.openpdf.examples.objects.tables.alternatives;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import org.openpdf.text.Cell;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Table;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Uses the old Table class to have rowspan and spacing.
 */
public class OldTable {

    /**
     * Demonstrates the features of the old Table class.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Old Table class");
        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2: creation of the writer-object
            PdfWriter.getInstance(document, new FileOutputStream("oldtable.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we create a table and add it to the document
            Table table = new Table(3);
            table.setBorderWidth(1);
            table.setBorderColor(new Color(0, 0, 255));
            table.setPadding(5);
            table.setSpacing(5);
            Cell cell = new Cell("header");
            cell.setHeader(true);
            cell.setColspan(3);
            table.addCell(cell);
            cell = new Cell("example cell with colspan 1 and rowspan 2");
            cell.setRowspan(2);
            cell.setBorderColor(new Color(255, 0, 0));
            table.addCell(cell);
            table.addCell("1.1");
            table.addCell("2.1");
            table.addCell("1.2");
            table.addCell("2.2");
            table.addCell("cell test1");
            cell = new Cell("big cell");
            cell.setRowspan(2);
            cell.setColspan(2);
            cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
            table.addCell(cell);
            table.addCell("cell test2");
            document.add(table);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5: we close the document
        document.close();
    }
}
