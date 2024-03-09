/*
 * $Id: WriteSelectedRows.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.tables.pdfptable;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Writing a PdfPTable at an absolute position.
 */
public class WriteSelectedRows {

    /**
     * Demonstrates the writeSelectedRows method.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("writeSelectedRows");
        // step1
        Document document = new Document(PageSize.A4, 2, 2, 2, 2);
        try {
            // step2
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("WriteSelectedRows.pdf"));
            // step3
            document.open();
            // step4
            PdfPTable table = new PdfPTable(4);
            table.getDefaultCell().setBorder(Rectangle.LEFT | Rectangle.RIGHT);
            for (int k = 0; k < 24; ++k) {
                table.addCell("cell " + k);
            }
            table.setTotalWidth(300f);
            table.writeSelectedRows(0, -1, 100, 600, writer.getDirectContent());
            document.newPage();
            document.add(table);
            document.newPage();
            table = new PdfPTable(2);
            float[] rows = {50f, 250f};
            table.setTotalWidth(rows);
            for (int k = 0; k < 200; ++k) {
                table.addCell("row " + k);
                table.addCell("blah blah blah " + k);
            }
            document.add(new Paragraph("row 0 - 50"));
            table.writeSelectedRows(0, 50, 50, 820, writer.getDirectContent());
            document.newPage();
            document.add(new Paragraph("row 50 - 100"));
            table.writeSelectedRows(50, 100, 50, 820, writer.getDirectContent());
            document.newPage();
            document.add(new Paragraph("row 100 - 150 DOESN'T FIT ON THE PAGE!!!"));
            table.writeSelectedRows(100, 150, 50, 200, writer.getDirectContent());
            document.newPage();
            document.add(new Paragraph("row 150 - 200"));
            table.writeSelectedRows(150, -1, 50, 820, writer.getDirectContent());
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }
}