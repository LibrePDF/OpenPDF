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
package org.openpdf.examples.objects.tables.pdfptable;

import java.io.FileOutputStream;
import org.openpdf.text.Document;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPCellEvent;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

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
     * @see org.openpdf.text.pdf.PdfPCellEvent#cellLayout(org.openpdf.text.pdf.PdfPCell, org.openpdf.text.Rectangle,
     * org.openpdf.text.pdf.PdfContentByte[])
     */
    public void cellLayout(PdfPCell cell, Rectangle position,
            PdfContentByte[] canvases) {
        PdfContentByte cb = canvases[PdfPTable.TEXTCANVAS];
        cb.moveTo(position.getLeft(), position.getBottom());
        cb.lineTo(position.getRight(), position.getTop());
        cb.stroke();
    }
}
