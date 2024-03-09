/*
 * $Id: CellWidths.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * Define the widths of the columns of a PdfPTable.
 */
public class CellWidths {

    /**
     * Width manipulations of cells.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Width");
        // step1
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            // step2
            PdfWriter.getInstance(document,
                    new FileOutputStream("CellWidths.pdf"));
            // step3
            document.open();
            // step4
            float[] widths = {0.1f, 0.1f, 0.05f, 0.75f};
            PdfPTable table = new PdfPTable(widths);
            table.addCell("10%");
            table.addCell("10%");
            table.addCell("5%");
            table.addCell("75%");
            table.addCell("aa");
            table.addCell("aa");
            table.addCell("a");
            table.addCell("aaaaaaaaaaaaaaa");
            table.addCell("bb");
            table.addCell("bb");
            table.addCell("b");
            table.addCell("bbbbbbbbbbbbbbb");
            table.addCell("cc");
            table.addCell("cc");
            table.addCell("c");
            table.addCell("ccccccccccccccc");
            document.add(table);
            document.add(new Paragraph("We change the percentages:\n\n"));
            widths[0] = 20f;
            widths[1] = 20f;
            widths[2] = 10f;
            widths[3] = 50f;
            table.setWidths(widths);
            document.add(table);
            widths[0] = 40f;
            widths[1] = 40f;
            widths[2] = 20f;
            widths[3] = 300f;
            Rectangle r = new Rectangle(PageSize.A4.getRight(72), PageSize.A4.getTop(72));
            table.setWidthPercentage(widths, r);
            document.add(new Paragraph("We change the percentage using absolute widths:\n\n"));
            document.add(table);
            document.add(new Paragraph("We use a locked width:\n\n"));
            table.setTotalWidth(300);
            table.setLockedWidth(true);
            document.add(table);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }
}