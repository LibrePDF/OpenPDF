/*
 * $Id: PaddingBorders.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Changing the padding and the borders of table cells.
 */
public class PaddingBorders {

    /**
     * Creating tables with different borders and padding.
     *
     * @param args no arguments needed.
     */
    public static void main(String[] args) {
        System.out.println("Old Table class");
        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2: creation of the writer-object
            PdfWriter.getInstance(document, new FileOutputStream("paddingborders.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we create a table and add it to the document
            Table table = new Table(3);
            table.setBorderWidth(1);
            table.setBorderColor(new Color(0, 0, 255));
            table.setPadding(10);
            Cell cell = new Cell("header");
            cell.setHeader(true);
            cell.setColspan(3);
            table.addCell(cell);
            table.addCell("1.1");
            table.addCell("2.1");
            table.addCell("3.1");
            table.addCell("1.2");
            table.addCell("2.2");
            table.addCell("3.2");
            document.add(table);
            table.setConvert2pdfptable(true);
            document.add(new Paragraph("converted to PdfPTable:"));
            document.add(table);

            table = new Table(3);
            table.setBorderWidth(3);
            table.setBorderColor(new Color(255, 0, 0));
            table.setPadding(0);
            cell = new Cell("header");
            cell.setHeader(true);
            cell.setBorderColorBottom(new Color(0, 0, 255));
            cell.setColspan(3);
            table.addCell(cell);
            table.addCell("1.1");
            cell = new Cell("2.1");
            cell.setBorderWidthLeft(4);
            cell.setBorderWidthRight(8);
            cell.setBorderWidthTop(2);
            cell.setBorderWidthBottom(10);
            cell.setUseBorderPadding(true);
            cell.setBorderColorBottom(new Color(0, 255, 0));
            table.addCell(cell);
            table.addCell("3.1");
            table.addCell("1.2");
            table.addCell("2.2");
            table.addCell("3.2");
            document.add(table);
            table.setConvert2pdfptable(true);
            document.add(new Paragraph("converted to PdfPTable:"));
            document.add(table);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5: we close the document
        document.close();
    }
}
