/*
 * $Id: CellEvents.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;

/**
 * General example using CellEvents.
 */
public class CellEvents implements PdfPCellEvent {

    /**
     * General example using cell events.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("CellEvents");
        // step1
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step2
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("CellEvents.pdf"));
            // step3
            document.open();
            // step4
            CellEvents event = new CellEvents();
            Image im = Image.getInstance("otsoe.jpg");
            im.setRotationDegrees(30);
            PdfPTable table = new PdfPTable(4);
            table.addCell("text 1");
            PdfPCell cell = new PdfPCell(im, true);
            cell.setCellEvent(event);
            table.addCell(cell);
            table.addCell("text 3");
            im.setRotationDegrees(0);
            table.addCell(im);
            table.setTotalWidth(300);
            PdfContentByte cb = writer.getDirectContent();
            table.writeSelectedRows(0, -1, 50, 600, cb);
            table.setHeaderRows(3);
            document.add(table);
        } catch (Exception de) {
            de.printStackTrace();
        }
        // step5
        document.close();
    }

    /**
     * @see com.lowagie.text.pdf.PdfPCellEvent#cellLayout(com.lowagie.text.pdf.PdfPCell, com.lowagie.text.Rectangle,
     * com.lowagie.text.pdf.PdfContentByte[])
     */
    public void cellLayout(PdfPCell cell, Rectangle position,
            PdfContentByte[] canvases) {
        PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
        cb.moveTo(position.getLeft(), position.getBottom());
        cb.lineTo(position.getRight(), position.getTop());
        cb.stroke();
    }
}