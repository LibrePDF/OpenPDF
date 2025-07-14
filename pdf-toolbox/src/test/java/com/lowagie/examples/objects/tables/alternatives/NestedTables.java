/*
 * $Id: NestedTables.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Point;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Nested tables.
 */
public class NestedTables {

    /**
     * Nested tables.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Nested tables");
        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2: creation of the writer
            PdfWriter.getInstance(document, new FileOutputStream("nestedtables.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we create a table and add it to the document

            // example 1

            Table secondTable = new Table(2);
            secondTable.addCell("2nd table 0.0");
            secondTable.addCell("2nd table 0.1");
            secondTable.addCell("2nd table 1.0");
            secondTable.addCell("2nd table 1.1");

            Table aTable = new Table(4, 4);    // 4 rows, 4 columns
            aTable.setAutoFillEmptyCells(true);
            aTable.addCell("2.2", new Point(2, 2));
            aTable.addCell("3.3", new Point(3, 3));
            aTable.addCell("2.1", new Point(2, 1));
            aTable.insertTable(secondTable, new Point(1, 3));
            document.add(aTable);
            document.add(new Paragraph("converted to PdfPTable:"));
            aTable.setConvert2pdfptable(true);
            document.add(aTable);
            document.newPage();

            // example 2

            Table thirdTable = new Table(2);
            thirdTable.addCell("3rd table 0.0");
            thirdTable.addCell("3rd table 0.1");
            thirdTable.addCell("3rd table 1.0");
            thirdTable.addCell("3rd table 1.1");

            aTable = new Table(5, 5);
            aTable.setAutoFillEmptyCells(true);
            aTable.addCell("2.2", new Point(2, 2));
            aTable.addCell("3.3", new Point(3, 3));
            aTable.addCell("2.1", new Point(2, 1));
            aTable.insertTable(secondTable, new Point(1, 3));
            aTable.insertTable(thirdTable, new Point(6, 2));
            document.add(aTable);
            document.add(new Paragraph("converted to PdfPTable:"));
            aTable.setConvert2pdfptable(true);
            document.add(aTable);
            document.newPage();

            // example 3
            aTable = new Table(3);
            float[] widths = {1, 2, 1};
            aTable.setWidths(widths);
            aTable.addCell("1.1");
            aTable.addCell("1.2");
            aTable.addCell("1.3");
            // nested
            Table t2 = new Table(2);
            t2.addCell("2.1");
            t2.addCell("2.2");

            // now insert the nested
            aTable.insertTable(t2);
            aTable.addCell("new cell");
            document.add(aTable);
            document.add(new Paragraph("converted to PdfPTable:"));
            aTable.setConvert2pdfptable(true);
            document.add(aTable);
            document.newPage();

            // relative column widths are preserved

            Table a = new Table(2);
            a.setWidths(new float[]{85, 15});
            a.addCell("a-1");
            a.addCell("a-2");

            Table b = new Table(5);
            b.setWidths(new float[]{15, 7, 7, 7, 7});
            b.addCell("b-1");
            b.addCell("b-2");
            b.addCell("b-3");
            b.addCell("b-4");
            b.addCell("b-5");

            // now, insert these 2 tables into a third for layout purposes
            Table c = new Table(3, 1);
            c.setWidth(100.0f);
            c.setWidths(new float[]{20, 2, 78});
            c.insertTable(a, new Point(0, 0));
            c.insertTable(b, new Point(0, 2));

            document.add(c);
            document.add(new Paragraph("converted to PdfPTable:"));
            c.setConvert2pdfptable(true);
            document.add(c);

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5: we close the document
        document.close();
    }
}
