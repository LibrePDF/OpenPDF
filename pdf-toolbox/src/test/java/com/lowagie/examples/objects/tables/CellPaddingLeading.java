/*
 * $Id: CellPaddingLeading.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Changing the padding and the leading of the content of a PdfPCell.
 */
public class CellPaddingLeading {

    /**
     * Changing padding and leading.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("padding - leading");
        // step1
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        try {
            // step2
            PdfWriter.getInstance(document,
                    new FileOutputStream("PaddingLeading.pdf"));
            // step3
            document.open();
            // step4
            PdfPTable table = new PdfPTable(2);
            PdfPCell cell;
            Paragraph p = new Paragraph(
                    "Quick brown fox jumps over the lazy dog. Quick brown fox jumps over the lazy dog.");
            table.addCell("default");
            table.addCell(p);
            table.addCell("padding 10");
            cell = new PdfPCell(p);
            cell.setPadding(10f);
            table.addCell(cell);
            table.addCell("no padding at all");
            cell = new PdfPCell(p);
            cell.setPadding(0f);
            table.addCell(cell);
            table.addCell("no padding at the top; large padding at the left");
            cell = new PdfPCell(p);
            cell.setPaddingTop(0f);
            cell.setPaddingLeft(20f);
            table.addCell(cell);
            document.add(table);

            document.newPage();
            table = new PdfPTable(2);
            table.addCell("no leading at all");
            table.getDefaultCell().setLeading(0f, 0f);
            table.addCell(
                    "blah blah\nblah blah blah\nblah blah\nblah blah blah\nblah blah\nblah blah blah\nblah blah\nblah blah blah\n");
            table.getDefaultCell().setLeading(14f, 0f);
            table.addCell("fixed leading of 14pt");
            table.addCell(
                    "blah blah\nblah blah blah\nblah blah\nblah blah blah\nblah blah\nblah blah blah\nblah blah\nblah blah blah\n");
            table.addCell("relative leading of 1.0 times the fontsize");
            table.getDefaultCell().setLeading(0f, 1.0f);
            table.addCell(
                    "blah blah\nblah blah blah\nblah blah\nblah blah blah\nblah blah\nblah blah blah\nblah blah\nblah blah blah\n");
            document.add(table);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }
}