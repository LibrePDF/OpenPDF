/*
 * $Id: MyFirstTable.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.tables;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A very simple PdfPTable example.
 */
public class MyFirstTable {

    /**
     * A very simple PdfPTable example.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("My First PdfPTable");

        // step 1: creation of a document-object
        Document document = new Document();

        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document, new FileOutputStream("MyFirstTable.pdf"));

            // step 3: we open the document
            document.open();

            PdfPTable table = new PdfPTable(3);
            PdfPCell cell = new PdfPCell(new Paragraph("header with colspan 3"));
            cell.setColspan(3);
            table.addCell(cell);
            table.addCell("1.1");
            table.addCell("2.1");
            table.addCell("3.1");
            table.addCell("1.2");
            table.addCell("2.2");
            table.addCell("3.2");
            cell = new PdfPCell(new Paragraph("cell test1"));
            cell.setBorderColor(new Color(255, 0, 0));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("cell test2"));
            cell.setColspan(2);
            cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
            table.addCell(cell);
            document.add(table);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}