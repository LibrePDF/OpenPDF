/*
 * $Id: TableWithImage.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A very simple Table example.
 */
public class TableWithImage {

    /**
     * A very simple PdfPTable example.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("A table with Image");
        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document,
                    new FileOutputStream("imageTable.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we create a table and add it to the document
            Table table = new Table(2, 2); // 2 rows, 2 columns
            table.addCell(new Cell(Image.getInstance("otsoe.jpg")));
            table.addCell(new Cell(Image.getInstance("iText.gif")));
            Cell c1 = new Cell();
            c1.add(Image.getInstance("iText.gif"));
            table.addCell(c1);
            Cell c2 = new Cell();
            c2.add(Image.getInstance("otsoe.jpg"));
            table.addCell(c2);
            document.add(table);
            document.add(new Paragraph("converted to PdfPTable:"));
            table.setConvert2pdfptable(true);
            document.add(table);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5: we close the document
        document.close();
    }
}