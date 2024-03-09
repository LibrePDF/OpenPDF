/*
 * $Id: CellHeights.java 3373 2008-05-12 16:21:24Z xlv $
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
 * Change the height of a cell (fixed height), disable text wrapping, set a minimum height.
 */
public class CellHeights {

    /**
     * Height manipulations of cells.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Height");
        // step1
        Document document = new Document(PageSize.A4);
        try {
            // step2
            PdfWriter.getInstance(document,
                    new FileOutputStream("CellHeights.pdf"));
            // step3
            document.open();
            // step4
            PdfPTable table = new PdfPTable(2);
            table.setExtendLastRow(true);
            PdfPCell cell;

            // wrap / nowrap
            cell = new PdfPCell(new Paragraph(
                    "blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah"));
            table.addCell("wrap");
            cell.setNoWrap(false);
            table.addCell(cell);
            table.addCell("no wrap");
            cell.setNoWrap(true);
            table.addCell(cell);

            // height
            cell = new PdfPCell(new Paragraph(
                    "1. blah blah\n2. blah blah blah\n3. blah blah\n4. blah blah blah\n5. blah blah\n6. blah blah blah\n7. blah blah\n8. blah blah blah"));
            table.addCell("height");
            table.addCell(cell);
            table.addCell("fixed height");
            cell.setFixedHeight(50f);
            table.addCell(cell);
            table.addCell("minimum height");
            cell = new PdfPCell(new Paragraph("x"));
            cell.setMinimumHeight(50f);
            table.addCell(cell);
            table.addCell("extend last row");
            cell = new PdfPCell(new Paragraph("almost no content, but the row is extended"));
            table.addCell(cell);
            document.add(table);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }
}