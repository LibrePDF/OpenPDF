/*
 * $Id: CellColors.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;

/**
 * The Cell is derived from the Rectangle object, so we can do all kinds of things with the borders and colors.
 */
public class CellColors {

    /**
     * Rectangle operations.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Rectangle methods on PdfPCell");
        // step1
        Document document = new Document(PageSize.A4);
        try {
            // step2
            PdfWriter.getInstance(document,
                    new FileOutputStream("CellColors.pdf"));
            // step3
            document.open();
            // step4
            PdfPTable table = new PdfPTable(4);
            PdfPCell cell;
            cell = new PdfPCell(new Paragraph("test colors:"));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("red"));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setBackgroundColor(Color.red);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("green"));
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColorBottom(Color.magenta);
            cell.setBorderWidthBottom(10f);
            cell.setBackgroundColor(Color.green);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("blue"));
            cell.setBorder(Rectangle.TOP);
            cell.setUseBorderPadding(true);
            cell.setBorderWidthTop(5f);
            cell.setBorderColorTop(Color.cyan);
            cell.setBackgroundColor(Color.blue);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("test GrayFill:"));
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("0.25"));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setGrayFill(0.25f);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("0.5"));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setGrayFill(0.5f);
            table.addCell(cell);
            cell = new PdfPCell(new Paragraph("0.75"));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setGrayFill(0.75f);
            table.addCell(cell);
            document.add(table);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }
}