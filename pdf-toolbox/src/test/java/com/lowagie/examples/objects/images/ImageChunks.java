/*
 * $Id: ImageChunks.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.images;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Wrapping Images in a Chunk.
 */
public class ImageChunks {

    /**
     * Images wrapped in a Chunk.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        Document.compress = false;
        System.out.println("images wrapped in a Chunk");
        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter.getInstance(document, new FileOutputStream("imageChunks.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we create a table and add it to the document
            Image img = Image.getInstance("pngnow.png");
            img.scalePercent(70);
            Chunk ck = new Chunk(img, 0, -5);
            PdfPTable table = new PdfPTable(3);
            PdfPCell cell = new PdfPCell();
            cell.addElement(new Chunk(img, 5, -5));
            cell.setBackgroundColor(new Color(0xC0, 0xC0, 0xC0));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell("I see an image\non my right");
            table.addCell(cell);
            table.addCell("I see an image\non my left");
            table.addCell(cell);
            table.addCell("I see images\neverywhere");
            table.addCell(cell);
            table.addCell("I see an image\non my right");
            table.addCell(cell);
            table.addCell("I see an image\non my left");

            Phrase p1 = new Phrase("This is an image ");
            p1.add(ck);
            p1.add(" just here.");
            document.add(p1);
            document.add(p1);
            document.add(p1);
            document.add(p1);
            document.add(p1);
            document.add(p1);
            document.add(p1);
            document.add(table);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5: we close the document
        document.close();
    }
}
