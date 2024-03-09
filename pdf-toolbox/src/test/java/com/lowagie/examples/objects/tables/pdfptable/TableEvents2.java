/*
 * $Id: TableEvents2.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * General example using TableEvents (with colspan).
 */
public class TableEvents2 implements PdfPTableEvent {

    /**
     * General example using table events (with colspan).
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Table Events 2");
        // step1
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("TableEvents2.pdf"));
            // step3
            document.open();
            // step4
            PdfPTable table = new PdfPTable(4);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            for (int k = 0; k < 24; ++k) {
                if (k != 0) {
                    table.addCell(String.valueOf(k));
                } else {
                    table.getDefaultCell().setColspan(3);
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell("This is a very big URL");
                    table.getDefaultCell().setColspan(1);
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    k += 2;
                }
            }
            TableEvents2 event = new TableEvents2();
            table.setTableEvent(event);

            // add the table with document add
            document.add(table);
            // add the table at an absolute position
            table.setTotalWidth(300);
            table.writeSelectedRows(0, -1, 100, 600, writer.getDirectContent());
            document.newPage();

            table = new PdfPTable(4);
            float fontSize = 12;
            BaseFont bf = BaseFont.createFont("Helvetica", "winansi", false);
            table.getDefaultCell().setPaddingTop(bf.getFontDescriptor(BaseFont.ASCENT, fontSize) - fontSize + 2);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            for (int k = 0; k < 500 * 4; ++k) {
                if (k == 0) {
                    table.getDefaultCell().setColspan(4);
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(new Phrase("This is an URL", new Font(bf, fontSize * 2)));
                    table.getDefaultCell().setColspan(1);
                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    k += 3;
                } else {
                    table.addCell(new Phrase(String.valueOf(k), new Font(bf, fontSize)));
                }
            }
            table.setTableEvent(event);
            table.setHeaderRows(3);
            document.add(table);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }

    /**
     * @see com.lowagie.text.pdf.PdfPTableEvent#tableLayout(com.lowagie.text.pdf.PdfPTable, float[][], float[], int,
     * int, com.lowagie.text.pdf.PdfContentByte[])
     */
    public void tableLayout(PdfPTable table, float[][] width, float[] heights, int headerRows, int rowStart,
            PdfContentByte[] canvases) {

        // widths of the different cells of the first row
        float[] widths = width[0];

        PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
        cb.saveState();
        // border for the complete table
        cb.setLineWidth(2);
        cb.setRGBColorStroke(255, 0, 0);
        cb.rectangle(widths[0], heights[heights.length - 1], widths[widths.length - 1] - widths[0],
                heights[0] - heights[heights.length - 1]);
        cb.stroke();

        // border for the header rows
        if (headerRows > 0) {
            float headerHeight = heights[0];
            for (int k = 0; k < headerRows; ++k) {
                headerHeight += heights[k];
            }
            cb.setRGBColorStroke(0, 0, 255);
            cb.rectangle(widths[0], heights[headerRows], widths[widths.length - 1] - widths[0],
                    heights[0] - heights[headerRows]);
            cb.stroke();
        }
        cb.restoreState();

        cb = canvases[PdfPTable.BASECANVAS];
        cb.saveState();
        // border for the cells
        cb.setLineWidth(.5f);
        // loop over the rows
        for (int line = 0; line < heights.length - 1; ++line) {
            widths = width[line];
            // loop over the columns
            for (int col = 0; col < widths.length - 1; ++col) {
                if (line == 0 && col == 0) {
                    cb.setAction(new PdfAction("https://github.com/LibrePDF/OpenPDF"),
                            widths[col], heights[line + 1], widths[col + 1], heights[line]);
                }
                cb.setRGBColorStrokeF((float) Math.random(), (float) Math.random(), (float) Math.random());
                // horizontal borderline
                cb.moveTo(widths[col], heights[line]);
                cb.lineTo(widths[col + 1], heights[line]);
                cb.stroke();
                // vertical borderline
                cb.setRGBColorStrokeF((float) Math.random(), (float) Math.random(), (float) Math.random());
                cb.moveTo(widths[col], heights[line]);
                cb.lineTo(widths[col], heights[line + 1]);
                cb.stroke();
            }
        }
        cb.restoreState();
    }
}